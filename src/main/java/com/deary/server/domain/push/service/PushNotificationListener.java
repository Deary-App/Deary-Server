package com.deary.server.domain.push.service;

import com.deary.server.domain.correction.event.CorrectionCompletedEvent;
import com.deary.server.domain.correction.event.CorrectionReactionCreatedEvent;
import com.deary.server.domain.profile.entity.Profile;
import com.deary.server.domain.profile.service.ProfileService;
import com.deary.server.domain.push.client.ExpoPushClient;
import com.deary.server.domain.push.client.ExpoPushClient.ExpoPushMessage;
import com.deary.server.domain.push.client.ExpoPushClient.SendResult;
import com.deary.server.domain.push.entity.PushToken;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 교정 도착·고마워요를 받는 사람의 기기로 푸시 알림을 보낸다. 커밋된 뒤 따로 보내서 실패해도 원래 요청에는 영향이 없다.
 * data.type과 diaryId로 앱이 알림을 눌렀을 때 열 화면을 정한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PushNotificationListener {

	private static final String TYPE_CORRECTION = "CORRECTION";
	private static final String TYPE_CORRECTION_REACTION = "CORRECTION_REACTION";

	private final PushTokenService pushTokenService;
	private final PushTicketService pushTicketService;
	private final ProfileService profileService;
	private final ExpoPushClient expoPushClient;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void notifyCorrectionCompleted(CorrectionCompletedEvent event) {
		send(event.authorId(), event.correctorId(), PushMessage.CORRECTION_COMPLETED,
				Map.of("type", TYPE_CORRECTION, "diaryId", event.diaryId()));
	}

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void notifyCorrectionReactionCreated(CorrectionReactionCreatedEvent event) {
		send(event.correctorId(), event.senderId(), PushMessage.CORRECTION_THANKED,
				Map.of("type", TYPE_CORRECTION_REACTION, "diaryId", event.diaryId()));
	}

	private void send(Long recipientId, Long actorId, PushMessage message, Map<String, Object> data) {
		if (!expoPushClient.enabled()) {
			return;
		}
		try {
			List<PushToken> tokens = pushTokenService.findAllPushToken(recipientId);
			if (tokens.isEmpty()) {
				return;
			}
			String name = nickname(actorId);
			List<ExpoPushMessage> messages = tokens.stream()
					.map(token -> new ExpoPushMessage(
							token.getToken(),
							message.title(token.getLanguage()),
							message.body(token.getLanguage(), name),
							data,
							"default"))
					.toList();
			SendResult result = expoPushClient.send(messages);
			pushTokenService.removeAllExpiredPushToken(result.expiredTokens());
			pushTicketService.registerAllPushTicket(result.acceptedTickets());
			log.info("푸시 알림 전송 type={} recipientId={} devices={} expired={}",
					data.get("type"), recipientId, messages.size(), result.expiredTokens().size());
		} catch (RuntimeException exception) {
			log.warn("푸시 알림 전송 실패 type={} recipientId={} error={}", data.get("type"), recipientId, exception.getClass().getSimpleName());
		}
	}

	private String nickname(Long userId) {
		Profile profile = profileService.findAllProfileById(List.of(userId)).get(userId);
		return profile == null ? null : profile.getNickname();
	}

}
