package com.aman.backend.model;

public record TestCase(
		String id,
		String name,
		TestCategory category,
		TestPriority priority,
		String description,
		TestRequest request,
		Integer expectedStatusCode,
		String reason
) {
}
