package com.aman.backend.model;

public record AiTestValidationResult(
		boolean valid,
		String reason
) {
}
