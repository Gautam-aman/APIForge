package com.aman.backend.model;

import java.util.List;

public record ApiEndpoint(
		String path,
		String method,
		String summary,
		List<String> responseCodes
) {
}
