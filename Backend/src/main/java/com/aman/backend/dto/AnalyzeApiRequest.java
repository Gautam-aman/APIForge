package com.aman.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalyzeApiRequest(
		@NotBlank(message = "API definition cannot be empty")
		String apiDefinition
) {
}
