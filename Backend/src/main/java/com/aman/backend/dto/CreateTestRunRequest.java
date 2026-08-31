package com.aman.backend.dto;

import java.util.List;

import com.aman.backend.model.TestCase;
import jakarta.validation.constraints.NotBlank;

public record CreateTestRunRequest(
		@NotBlank(message = "Base URL cannot be empty")
		String baseUrl,
		String apiDefinition,
		List<TestCase> testCases
) {
}
