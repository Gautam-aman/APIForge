package com.aman.backend.model;

public record AssertionResult(
		boolean passed,
		AssertionType assertionType,
		String expected,
		String actual,
		String message
) {
}
