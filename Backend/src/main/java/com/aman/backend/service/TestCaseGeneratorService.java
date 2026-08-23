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
			testCases.addAll(generateForEndpoint(endpoint));
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

	private void generateRequiredFieldTests(ApiEndpoint endpoint, List<TestCase> testCases) {

		if (endpoint.requestBody() == null) {
			return;
		}

		for (ApiField field : endpoint.requestBody().fields()) {
			if (!field.required()) {
				continue;
			}
			Object validBody = createValidBody(endpoint);

			if (!(validBody instanceof Map<?, ?>)) {
				continue;
			}

			Map<String, Object> invalidBody = new HashMap<>((Map<String, Object>) validBody);
			invalidBody.remove(field.name());

			TestRequest request =
					new TestRequest(
							endpoint.method(),
							endpoint.path(),
							Map.of(),
							Map.of(),
							Map.of(
									"Content-Type",
									"application/json"
							),
							invalidBody
					);

			testCases.add(
					new TestCase(
							UUID.randomUUID().toString(),
							"Missing required field: " +
									field.name(),
							TestCategory.REQUIRED_FIELD,
							TestPriority.HIGH,
							"Verify that the API rejects a request when required field '" +
									field.name() +
									"' is missing.",
							request,
							400,
							"The OpenAPI schema marks this field as required."
					)
			);
		}
	}

	private void generateBoundaryTests(ApiEndpoint endpoint, List<TestCase> testCases) {
		if (endpoint.requestBody() == null) {
			return;
		}

		for (ApiField field : endpoint.requestBody().fields()) {

			if (field.minimum() != null) {
				addBoundaryTest(
						endpoint,
						field,
						field.minimum().subtract(
								java.math.BigDecimal.ONE
						),
						"below minimum",
						testCases
				);
			}

			if (field.maximum() != null) {
				addBoundaryTest(
						endpoint,
						field,
						field.maximum().add(
								java.math.BigDecimal.ONE
						),
						"above maximum",
						testCases
				);
			}

			if (field.minLength() != null) {
				String value =
						"x".repeat(
								Math.max(
										0,
										field.minLength() - 1
								)
						);

				addBoundaryTest(
						endpoint,
						field,
						value,
						"below minimum length",
						testCases
				);
			}

			if (field.maxLength() != null) {

				String value = "x".repeat(field.maxLength() + 1);

				addBoundaryTest(
						endpoint,
						field,
						value,
						"above maximum length",
						testCases
				);
			}
		}
	}

	private void addBoundaryTest(ApiEndpoint endpoint, ApiField field, Object invalidValue, String scenario, List<TestCase> testCases) {

		Object validBody = createValidBody(endpoint);

		if (!(validBody instanceof Map<?, ?>)) {
			return;
		}

		Map<String, Object> invalidBody = new HashMap<>((Map<String, Object>) validBody);
		invalidBody.put(
				field.name(),
				invalidValue
		);

		TestRequest request =
				new TestRequest(
						endpoint.method(),
						endpoint.path(),
						Map.of(),
						Map.of(),
						Map.of(
								"Content-Type",
								"application/json"
						),
						invalidBody
				);

		testCases.add(
				new TestCase(
						UUID.randomUUID().toString(),
						"Boundary test: " +
								field.name() +
								" - " +
								scenario,
						TestCategory.BOUNDARY,
						TestPriority.MEDIUM,
						"Verify validation boundary for field '" +
								field.name() +
								"'.",
						request,
						400,
						"The API schema defines a boundary that should be enforced."
				)
		);
	}

	private void generateFormatTests(ApiEndpoint endpoint, List<TestCase> testCases) {

		if (endpoint.requestBody() == null) {
			return;
		}

		for (ApiField field :
				endpoint.requestBody().fields()) {

			if ("email".equals(field.format())) {

				Object validBody = createValidBody(endpoint);

				if (!(validBody instanceof Map<?, ?>)) {
					continue;
				}

				Map<String, Object> invalidBody = new HashMap<>((Map<String, Object>) validBody);

				invalidBody.put(
						field.name(),
						"not-an-email"
				);
				TestRequest request =
						new TestRequest(
								endpoint.method(),
								endpoint.path(),
								Map.of(),
								Map.of(),
								Map.of(
										"Content-Type",
										"application/json"
								),
								invalidBody
						);

				testCases.add(
						new TestCase(
								UUID.randomUUID().toString(),
								"Invalid email: " +
										field.name(),
								TestCategory.FORMAT,
								TestPriority.MEDIUM,
								"Verify that invalid email format is rejected.",
								request,
								400,
								"OpenAPI declares this field as an email format."
						)
				);
			}
		}
	}

	private void generateAuthenticationTests(ApiEndpoint endpoint, List<TestCase> testCases) {
		if (endpoint.authentication() == null ||
				!endpoint.authentication().required()) {

			return;
		}

		TestRequest request =
				new TestRequest(
						endpoint.method(),
						endpoint.path(),
						Map.of(),
						Map.of(),
						Map.of(
								"Content-Type",
								"application/json"
						),
						createValidBody(endpoint)
				);

		testCases.add(new TestCase(
						UUID.randomUUID().toString(),
						"Missing authentication",
						TestCategory.AUTHENTICATION,
						TestPriority.HIGH,
						"Verify that the API rejects unauthenticated requests.",
						request,
						401,
						"The OpenAPI specification declares authentication for this endpoint."
				)
		);
	}

	public List<TestCase> generateForEndpoint(ApiEndpoint endpoint) {

		List<TestCase> testCases = new ArrayList<>();

		testCases.add(generateHappyPath(endpoint));
		generateRequiredFieldTests(
				endpoint,
				testCases
		);
		generateBoundaryTests(
				endpoint,
				testCases
		);
		generateFormatTests(
				endpoint,
				testCases
		);
		generateAuthenticationTests(
				endpoint,
				testCases
		);
		return testCases;
	}

}
