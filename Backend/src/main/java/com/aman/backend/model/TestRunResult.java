package com.aman.backend.model;

import java.util.List;

public record TestRunResult(
		int total,
		int passed,
		int failed,
		int errors,
		long durationMs,
		List<TestExecutionResult> results
) {
}
