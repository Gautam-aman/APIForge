package com.aman.backend.model;
import java.util.Map;

public record TestRequest(

		String method,
		String path,
		Map<String, Object> pathParameters,
		Map<String, Object> queryParameters,
		Map<String, Object> headers,
		Object body

) {
}
