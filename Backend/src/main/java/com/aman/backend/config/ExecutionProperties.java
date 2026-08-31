package com.aman.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apiforge.execution")
public record ExecutionProperties(
		long connectTimeoutMs,
		long requestTimeoutMs,
		int maxConcurrency,
		int maxTestsPerRun,
		int maxResponseBodyBytes

) {
}
