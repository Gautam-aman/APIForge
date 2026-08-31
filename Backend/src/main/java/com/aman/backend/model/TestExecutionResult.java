package com.aman.backend.model;

import java.util.List;

public record TestExecutionResult(
		String testId,
		String testName,
		TestStatus status,
		Integer expectedStatusCode,
		Integer actualStatusCode,
		long durationMs,
		String responseBody,
		String error,
		String explanation,
		List<AssertionResult> assertions

) {
}
