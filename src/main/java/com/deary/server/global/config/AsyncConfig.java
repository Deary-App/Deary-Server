package com.deary.server.global.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@Configuration
public class AsyncConfig {

	/**
	 * @Async 작업에 요청의 MDC(traceId)를 넘겨, 비동기 로그도 원래 요청의 traceId로 찾을 수 있게 한다.
	 */
	@Bean
	public TaskDecorator mdcTaskDecorator() {
		return runnable -> {
			Map<String, String> context = MDC.getCopyOfContextMap();
			return () -> {
				if (context == null) {
					MDC.clear();
				} else {
					MDC.setContextMap(context);
				}
				try {
					runnable.run();
				} finally {
					MDC.clear();
				}
			};
		};
	}

}
