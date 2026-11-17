package com.deary.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.deary.server.global.config.AsyncConfig;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

class AsyncMdcTest {

	private final TaskDecorator decorator = new AsyncConfig().mdcTaskDecorator();

	@AfterEach
	void tearDown() {
		MDC.clear();
	}

	@Test
	void 비동기_작업에_요청의_traceId가_전달된다() throws InterruptedException {
		MDC.put("traceId", "abc123");
		AtomicReference<String> seen = new AtomicReference<>();
		Runnable task = decorator.decorate(() -> seen.set(MDC.get("traceId")));

		Thread thread = new Thread(task);
		thread.start();
		thread.join();

		assertThat(seen.get()).isEqualTo("abc123");
	}

	@Test
	void 작업이_끝나면_작업_스레드의_MDC를_비운다() {
		AtomicReference<String> after = new AtomicReference<>();
		MDC.put("traceId", "abc123");
		Runnable task = decorator.decorate(() -> { });
		MDC.clear();

		task.run();
		after.set(MDC.get("traceId"));

		assertThat(after.get()).isNull();
	}

}
