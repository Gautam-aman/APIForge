package com.aman.backend.model;

import java.util.Map;

public record AiTestSuggestion(
		String name,
		TestCategory category,
		TestPriority priority,
		String description,
		Map<String, Object> pathParameters,
		Map<String, Object> queryParameters,
		Map<String, Object> headers,
		Object body,
		Integer expectedStatusCode,
		String reason
) {
}
