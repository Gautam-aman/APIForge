package com.aman.backend.model;

import java.util.List;

public record ApiRequestBody(
		boolean required,
		String contentType,
		List<ApiField> fields
) {
}
