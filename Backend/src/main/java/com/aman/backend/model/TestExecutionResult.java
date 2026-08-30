package com.aman.backend.model;


public record TestExecutionResult(
		String testId,
		String testName,
		TestStatus status,
		Integer expectedStatusCode,
		Integer actualStatusCode,
		long durationMs,
		String responseBody,
		String error,
		String explanation

) {
}
