package com.aman.backend.model;

import java.util.List;

public record ApiEndpoint(
		String path,
		String method,
		String summary,
		String description,
		List<ApiParameter> parameters,
		ApiRequestBody requestBody,
		List<ApiResponse> responses,
		AuthenticationInfo authentication
) {

}
