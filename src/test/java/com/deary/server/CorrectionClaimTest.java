package com.deary.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.deary.server.domain.correction.service.CorrectionService;
import com.deary.server.domain.correction.dto.response.CorrectionClaimResponse;
import com.deary.server.domain.correction.entity.CorrectionRequest;
import com.deary.server.domain.correction.entity.CorrectionRequestStatus;
import com.deary.server.domain.correction.exception.CorrectionErrorCode;
import com.deary.server.domain.correction.repository.CorrectionRequestRepository;
import com.deary.server.domain.diary.entity.Diary;
import com.deary.server.domain.diary.repository.DiaryRepository;
import com.deary.server.domain.profile.entity.Gender;
import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.profile.entity.LearningLevel;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.repository.ProfileRepository;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.UserRepository;
import com.deary.server.global.exception.CustomException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class CorrectionClaimTest {

	@Autowired
	private CorrectionService correctionService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private DiaryRepository diaryRepository;

	@Autowired
	private CorrectionRequestRepository correctionRequestRepository;

	@Test
	void 같은_일기를_다시_맡아도_만료_시각이_늘어나지_않는다() {
		User author = saveUser("author");
		User corrector = saveUser("corrector");
		Diary diary = saveDiary(author, 1);

		CorrectionClaimResponse first = correctionService.handleClaim(corrector.getId(), diary.getId());
		CorrectionClaimResponse second = correctionService.handleClaim(corrector.getId(), diary.getId());

		assertThat(second.claimExpiresAt()).isEqualTo(first.claimExpiresAt());
	}

	@Test
	void 다른_일기를_맡으면_전에_맡던_일기는_풀린다() {
		User author = saveUser("author");
		User corrector = saveUser("corrector");
		Diary firstDiary = saveDiary(author, 1);
		Diary secondDiary = saveDiary(author, 2);

		correctionService.handleClaim(corrector.getId(), firstDiary.getId());
		correctionService.handleClaim(corrector.getId(), secondDiary.getId());

		CorrectionRequest first = request(firstDiary);
		CorrectionRequest second = request(secondDiary);
		assertThat(first.getStatus()).isEqualTo(CorrectionRequestStatus.OPEN);
		assertThat(first.getClaimedBy()).isNull();
		assertThat(second.getStatus()).isEqualTo(CorrectionRequestStatus.CLAIMED);
		assertThat(second.getClaimedBy().getId()).isEqualTo(corrector.getId());
		assertThat(correctionRequestRepository.findAllCorrectionRequestByClaimedById(corrector.getId())).hasSize(1);
	}

	@Test
	void 풀린_일기는_다른_사람이_맡을_수_있다() {
		User author = saveUser("author");
		User corrector = saveUser("corrector");
		User other = saveUser("other");
		Diary firstDiary = saveDiary(author, 1);
		Diary secondDiary = saveDiary(author, 2);

		correctionService.handleClaim(corrector.getId(), firstDiary.getId());
		correctionService.handleClaim(corrector.getId(), secondDiary.getId());
		correctionService.handleClaim(other.getId(), firstDiary.getId());

		assertThat(request(firstDiary).getClaimedBy().getId()).isEqualTo(other.getId());
	}

	@Test
	void 다른_사람이_맡고_있는_일기는_맡을_수_없고_내_선점도_그대로다() {
		User author = saveUser("author");
		User corrector = saveUser("corrector");
		User other = saveUser("other");
		Diary mine = saveDiary(author, 1);
		Diary taken = saveDiary(author, 2);

		correctionService.handleClaim(corrector.getId(), mine.getId());
		correctionService.handleClaim(other.getId(), taken.getId());

		assertThatThrownBy(() -> correctionService.handleClaim(corrector.getId(), taken.getId()))
				.isInstanceOfSatisfying(CustomException.class,
						exception -> assertThat(exception.getErrorCode()).isEqualTo(CorrectionErrorCode.ALREADY_CLAIMED));
		assertThat(request(mine).getClaimedBy().getId()).isEqualTo(corrector.getId());
	}

	private User saveUser(String name) {
		User user = userRepository.save(User.create(name + "@example.com"));
		profileRepository.save(Profile.create(user, new Profile.ProfileInfo(
				name, LocalDate.of(2000, 1, 1), "KR", Language.KO, Language.EN, LearningLevel.MEDIUM, Gender.UNDISCLOSED, null)));
		return user;
	}

	private Diary saveDiary(User author, int daysAgo) {
		Diary diary = Diary.create(author, LocalDate.now().minusDays(daysAgo), "title", "Hello world.", Language.EN);
		diary.addSentence("Hello world.");
		diaryRepository.saveAndFlush(diary);
		correctionRequestRepository.saveAndFlush(CorrectionRequest.open(diary));
		return diary;
	}

	private CorrectionRequest request(Diary diary) {
		return correctionRequestRepository.findCorrectionRequestByDiaryId(diary.getId()).orElseThrow();
	}

}
