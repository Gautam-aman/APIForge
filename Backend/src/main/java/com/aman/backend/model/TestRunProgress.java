package com.aman.backend.model;

public record TestRunProgress(
		int total,
		int completed,
		int passed,
		int failed,
		int errors
) {
}
