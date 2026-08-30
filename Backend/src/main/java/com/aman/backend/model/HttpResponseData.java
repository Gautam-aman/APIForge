package com.aman.backend.model;
import java.util.*;

public record HttpResponseData(
		int statusCode,
		long durationMs,
		Map<String, String> headers,
		String body
) {
}
