package com.deary.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.deary.server.domain.push.client.ExpoPushClient;
import com.deary.server.domain.push.client.ExpoPushClient.AcceptedTicket;
import com.deary.server.domain.push.client.ExpoPushClient.ReceiptResult;
import com.deary.server.domain.push.entity.PushLanguage;
import com.deary.server.domain.push.entity.PushTicket;
import com.deary.server.domain.push.repository.PushTicketRepository;
import com.deary.server.domain.push.repository.PushTokenRepository;
import com.deary.server.domain.push.service.PushReceiptScheduler;
import com.deary.server.domain.push.service.PushTicketService;
import com.deary.server.domain.push.service.PushTokenService;
import com.deary.server.domain.user.entity.User;
import com.deary.server.domain.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@Transactional
class PushReceiptSchedulerTest {

	private static final String REMOVED_DEVICE_TOKEN = "ExponentPushToken[removed]";
	private static final String ACTIVE_DEVICE_TOKEN = "ExponentPushToken[active]";

	@MockitoBean
	private ExpoPushClient expoPushClient;

	@Autowired
	private PushReceiptScheduler pushReceiptScheduler;

	@Autowired
	private PushTicketService pushTicketService;

	@Autowired
	private PushTokenService pushTokenService;

	@Autowired
	private PushTokenRepository pushTokenRepository;

	@Autowired
	private PushTicketRepository pushTicketRepository;

	@Autowired
	private UserRepository userRepository;

	private User user;

	@BeforeEach
	void setUp() {
		given(expoPushClient.enabled()).willReturn(true);
		user = userRepository.save(User.create("user@example.com"));
		pushTokenService.registerPushToken(user.getId(), REMOVED_DEVICE_TOKEN, PushLanguage.KO);
		pushTokenService.registerPushToken(user.getId(), ACTIVE_DEVICE_TOKEN, PushLanguage.KO);
		pushTicketService.registerAllPushTicket(List.of(
				new AcceptedTicket("ticket-removed", REMOVED_DEVICE_TOKEN),
				new AcceptedTicket("ticket-active", ACTIVE_DEVICE_TOKEN),
				new AcceptedTicket("ticket-pending", ACTIVE_DEVICE_TOKEN)));
	}

	@Test
	void 등록되지_않은_기기로_확인된_토큰을_지우고_결과가_나온_접수표만_지운다() {
		given(expoPushClient.findAllReceipt(anyList())).willReturn(new ReceiptResult(
				Set.of("ticket-removed", "ticket-active"), Set.of("ticket-removed")));

		pushReceiptScheduler.handleReceiptCheck(Instant.now().plus(Duration.ofMinutes(20)));

		assertThat(pushTokenRepository.findPushTokenByToken(REMOVED_DEVICE_TOKEN)).isEmpty();
		assertThat(pushTokenRepository.findPushTokenByToken(ACTIVE_DEVICE_TOKEN)).isPresent();
		assertThat(pushTicketRepository.findAll()).extracting(PushTicket::getTicketId).containsExactly("ticket-pending");
	}

	@Test
	void 푸시를_보낸_뒤_다시_등록한_토큰은_예전_결과로_지우지_않는다() throws InterruptedException {
		Thread.sleep(5);
		pushTokenService.registerPushToken(user.getId(), REMOVED_DEVICE_TOKEN, PushLanguage.KO);
		given(expoPushClient.findAllReceipt(anyList())).willReturn(new ReceiptResult(
				Set.of("ticket-removed"), Set.of("ticket-removed")));

		pushReceiptScheduler.handleReceiptCheck(Instant.now().plus(Duration.ofMinutes(20)));

		assertThat(pushTokenRepository.findPushTokenByToken(REMOVED_DEVICE_TOKEN)).isPresent();
		assertThat(pushTicketRepository.findAll()).extracting(PushTicket::getTicketId)
				.containsExactlyInAnyOrder("ticket-active", "ticket-pending");
	}

	@Test
	void 보관_기간이_지난_접수표는_결과를_조회하지_않고_지운다() {
		pushReceiptScheduler.handleReceiptCheck(Instant.now().plus(Duration.ofHours(25)));

		assertThat(pushTicketRepository.findAll()).isEmpty();
		assertThat(pushTokenRepository.findAll()).hasSize(2);
		verify(expoPushClient, never()).findAllReceipt(anyList());
	}

	@Test
	void 결과_조회가_실패해도_예외를_던지지_않고_접수표와_토큰을_그대로_둔다() {
		given(expoPushClient.findAllReceipt(anyList())).willThrow(new IllegalStateException("expo down"));

		pushReceiptScheduler.handleReceiptCheck(Instant.now().plus(Duration.ofMinutes(20)));

		assertThat(pushTicketRepository.findAll()).hasSize(3);
		assertThat(pushTokenRepository.findAll()).hasSize(2);
	}

	@Test
	void 보낸_지_15분이_안_된_접수표는_조회하지_않는다() {
		pushReceiptScheduler.handleReceiptCheck(Instant.now());

		assertThat(pushTicketRepository.findAll()).hasSize(3);
		assertThat(pushTokenRepository.findAll()).hasSize(2);
		verify(expoPushClient, never()).findAllReceipt(anyList());
	}

	@Test
	void 접수표가_1000건을_넘으면_나눠서_모두_조회한다() {
		pushTicketService.registerAllPushTicket(IntStream.range(0, 1000)
				.mapToObj(i -> new AcceptedTicket("bulk-" + i, ACTIVE_DEVICE_TOKEN))
				.toList());
		given(expoPushClient.findAllReceipt(anyList())).willAnswer(invocation -> {
			List<String> ids = invocation.getArgument(0);
			return new ReceiptResult(new HashSet<>(ids), Set.of());
		});

		pushReceiptScheduler.handleReceiptCheck(Instant.now().plus(Duration.ofMinutes(20)));

		verify(expoPushClient, times(2)).findAllReceipt(anyList());
		assertThat(pushTicketRepository.findAll()).isEmpty();
	}

}
