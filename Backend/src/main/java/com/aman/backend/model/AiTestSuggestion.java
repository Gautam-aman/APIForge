package com.aman.backend.model;

public record AiTestSuggestion(
		String name,
		TestCategory category,
		TestPriority priority,
		String description,
		String reason
) {
}
