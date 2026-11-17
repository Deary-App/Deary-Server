package com.deary.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.deary.server.domain.block.service.BlockService;
import com.deary.server.domain.correction.dto.request.CorrectionSaveRequest;
import com.deary.server.domain.correction.dto.response.CorrectionFindAllResponse;
import com.deary.server.domain.correction.entity.CorrectionReactionType;
import com.deary.server.domain.correction.entity.CorrectionRequest;
import com.deary.server.domain.correction.event.CorrectionReactionCreatedEvent;
import com.deary.server.domain.correction.repository.CorrectionRequestRepository;
import com.deary.server.domain.correction.service.CorrectionService;
import com.deary.server.domain.diary.entity.Diary;
import com.deary.server.domain.diary.repository.DiaryRepository;
import com.deary.server.domain.profile.entity.Gender;
import com.deary.server.domain.profile.entity.Language;
import com.deary.server.domain.profile.entity.LearningLevel;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.repository.ProfileRepository;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
@RecordApplicationEvents
class CorrectionReactionBlockTest {

	@Autowired
	private CorrectionService correctionService;

	@Autowired
	private BlockService blockService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProfileRepository profileRepository;

	@Autowired
	private DiaryRepository diaryRepository;

	@Autowired
	private CorrectionRequestRepository correctionRequestRepository;

	@Autowired
	private ApplicationEvents events;

	@Test
	void 차단_관계가_아니면_고마워요가_교정자에게_보이고_알림_이벤트가_발행된다() {
		User author = saveUser("author");
		User corrector = saveUser("corrector");
		Diary diary = saveCorrectedDiary(author, corrector);

		correctionService.generateCorrectionReaction(author.getId(), diary.getId(), CorrectionReactionType.THANKS);

		CorrectionFindAllResponse.Activity activity = onlyActivity(corrector);
		assertThat(activity.reactions()).containsExactly(CorrectionReactionType.THANKS);
		assertThat(activity.hasUnreadReaction()).isTrue();
		assertThat(correctionService.hasUnreadCorrectionReaction(corrector.getId())).isTrue();
		assertThat(events.stream(CorrectionReactionCreatedEvent.class)).hasSize(1);
	}

	@Test
	void 교정자가_작성자를_차단하면_고마워요가_보이지_않고_알림_이벤트도_없다() {
		User author = saveUser("author");
		User corrector = saveUser("corrector");
		Diary diary = saveCorrectedDiary(author, corrector);
		blockService.registerBlock(corrector.getId(), author.getId());

		correctionService.generateCorrectionReaction(author.getId(), diary.getId(), CorrectionReactionType.THANKS);

		CorrectionFindAllResponse.Activity activity = onlyActivity(corrector);
		assertThat(activity.reactions()).isEmpty();
		assertThat(activity.hasUnreadReaction()).isFalse();
		assertThat(correctionService.hasUnreadCorrectionReaction(corrector.getId())).isFalse();
		assertThat(events.stream(CorrectionReactionCreatedEvent.class)).isEmpty();
	}

	@Test
	void 작성자가_교정자를_차단해도_고마워요가_보이지_않고_알림_이벤트도_없다() {
		User author = saveUser("author");
		User corrector = saveUser("corrector");
		Diary diary = saveCorrectedDiary(author, corrector);
		blockService.registerBlock(author.getId(), corrector.getId());

		correctionService.generateCorrectionReaction(author.getId(), diary.getId(), CorrectionReactionType.THANKS);

		assertThat(onlyActivity(corrector).reactions()).isEmpty();
		assertThat(correctionService.hasUnreadCorrectionReaction(corrector.getId())).isFalse();
		assertThat(events.stream(CorrectionReactionCreatedEvent.class)).isEmpty();
	}

	private CorrectionFindAllResponse.Activity onlyActivity(User corrector) {
		List<CorrectionFindAllResponse.Activity> activities =
				correctionService.findAllMyCorrection(corrector.getId(), null, 10).corrections();
		assertThat(activities).hasSize(1);
		return activities.getFirst();
	}

	private User saveUser(String name) {
		User user = userRepository.save(User.create(name + "@example.com"));
		profileRepository.save(Profile.create(user, new Profile.ProfileInfo(
				name, LocalDate.of(2000, 1, 1), "KR", Language.KO, Language.EN, LearningLevel.MEDIUM, Gender.UNDISCLOSED, null)));
		return user;
	}

	private Diary saveCorrectedDiary(User author, User corrector) {
		Diary diary = Diary.create(author, LocalDate.now().minusDays(1), "title", "Hello world.", Language.EN);
		diary.addSentence("Hello world.");
		diaryRepository.saveAndFlush(diary);
		correctionRequestRepository.saveAndFlush(CorrectionRequest.open(diary));
		correctionService.handleClaim(corrector.getId(), diary.getId());
		Long sentenceId = diary.getSentences().getFirst().getId();
		correctionService.generateCorrection(corrector.getId(), diary.getId(), new CorrectionSaveRequest(
				List.of(new CorrectionSaveRequest.Item(sentenceId, false, null, null)), "좋아요"));
		return diary;
	}

}
