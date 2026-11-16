package com.deary.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.deary.server.domain.profile.entity.Gender;
import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.profile.entity.LearningLevel;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.repository.ProfileRepository;
import com.deary.server.domain.profile.service.ProfileService;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class PublicProfileAgeTest {

	private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Seoul"));

	@Autowired
	private ProfileService profileService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Test
	void 만_18세_미만이면_나이를_보여주지_않는다() {
		User viewer = saveUser("viewer", TODAY.minusYears(30));
		User minor = saveUser("minor", TODAY.minusYears(18).plusDays(1));

		assertThat(profileService.findPublicProfile(viewer.getId(), minor.getId()).age()).isNull();
	}

	@Test
	void 만_18세가_되는_날부터_나이를_보여준다() {
		User viewer = saveUser("viewer", TODAY.minusYears(30));
		User adult = saveUser("adult", TODAY.minusYears(18));

		assertThat(profileService.findPublicProfile(viewer.getId(), adult.getId()).age()).isEqualTo(18);
	}

	private User saveUser(String name, LocalDate birthDate) {
		User user = userRepository.save(User.create(name + "@example.com"));
		profileRepository.save(Profile.create(user, new Profile.ProfileInfo(
				name, birthDate, "KR", Language.KO, Language.EN, LearningLevel.MEDIUM, Gender.UNDISCLOSED, null)));
		return user;
	}

}
