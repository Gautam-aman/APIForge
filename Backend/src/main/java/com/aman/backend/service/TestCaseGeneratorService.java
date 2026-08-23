package com.aman.backend.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.aman.backend.model.ApiEndpoint;
import com.aman.backend.model.ApiField;
import com.aman.backend.model.ApiResponse;
import com.aman.backend.model.TestCase;
import com.aman.backend.model.TestCategory;
import com.aman.backend.model.TestPriority;
import com.aman.backend.model.TestRequest;

import org.springframework.stereotype.Service;

@Service
public class TestCaseGeneratorService {

	public List<TestCase> generate(List<ApiEndpoint> endpoints) {
		List<TestCase> testCases = new ArrayList<>();

		for (ApiEndpoint endpoint : endpoints) {

			testCases.add(generateHappyPath(endpoint));
			generateRequiredFieldTests(endpoint, testCases);
			generateBoundaryTests(endpoint, testCases);
			generateFormatTests(endpoint, testCases);
			generateAuthenticationTests(endpoint, testCases);
		}
		return testCases;
	}

	private TestCase generateHappyPath(ApiEndpoint endpoint) {
		return new TestCase(
				UUID.randomUUID().toString(),
				"Happy path - " +
						endpoint.method() +
						" " +
						endpoint.path(),
				TestCategory.HAPPY_PATH,
				TestPriority.HIGH,
				"Verify that the endpoint works with valid input.",
				createValidRequest(endpoint),
				findSuccessStatus(endpoint),
				"Every endpoint should have at least one valid request test."
		);
	}

	private TestRequest createValidRequest(ApiEndpoint endpoint) {

		Map<String, Object> pathParameters = new HashMap<>();
		Map<String, Object> queryParameters = new HashMap<>();

		Map<String, Object> headers = new HashMap<>();

		headers.put(
				"Content-Type",
				"application/json"
		);

		Object body = createValidBody(endpoint);

		return new TestRequest(
				endpoint.method(),
				endpoint.path(),
				pathParameters,
				queryParameters,
				headers,
				body
		);
	}

	private Object createValidBody(ApiEndpoint endpoint) {
		if (endpoint.requestBody() == null) {
			return null;
		}

		Map<String, Object> body = new HashMap<>();

		for (ApiField field : endpoint.requestBody().fields()) {
			body.put(
					field.name(),
					generateValidValue(field)
			);
		}
		return body;
	}

	private Object generateValidValue(ApiField field) {
		if (field.example() != null) {
			return field.example();
		}

		if (!field.enumValues().isEmpty()) {
			return field.enumValues().get(0);
		}

		return switch (field.type()) {
			case "string" -> {

				if ("email".equals(field.format())) {
					yield "user@example.com";
				}
				yield "test-value";
			}

			case "integer", "number" -> {
				if (field.minimum() != null) {
					yield field.minimum().intValue();
				}
				yield 1;
			}

			case "boolean" -> true;

			default -> null;
		};
	}

	private int findSuccessStatus(ApiEndpoint endpoint) {
		return endpoint.responses()
				.stream()
				.map(ApiResponse::statusCode)
				.filter(status -> status.startsWith("2"))
				.map(this::parseStatusCode)
				.findFirst()
				.orElse(200);
	}

	private int parseStatusCode(String statusCode) {
		try {
			return Integer.parseInt(statusCode);
		} catch (NumberFormatException exception) {
			return 200;
		}
	}
}
