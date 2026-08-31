package com.aman.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.aman.backend.model.ApiEndpoint;
import com.aman.backend.model.TestCase;
import com.aman.backend.model.TestCategory;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

class TestCaseGeneratorServiceTests {

	private final OpenApiParserService parserService = new OpenApiParserService();
	private final TestCaseGeneratorService generatorService = new TestCaseGeneratorService();

	@Test
	void generatesBaselineTestsFromParsedEndpointSchema() {
		OpenAPI openAPI = parserService.parse(OpenApiParserServiceTests.sampleOpenApi());
		ApiEndpoint endpoint = parserService.extractEndpoints(openAPI).getFirst();

		List<TestCase> testCases = generatorService.generateForEndpoint(endpoint);

		assertThat(testCases).extracting(TestCase::category)
				.contains(
						TestCategory.HAPPY_PATH,
						TestCategory.REQUIRED_FIELD,
						TestCategory.BOUNDARY,
						TestCategory.FORMAT,
						TestCategory.AUTHENTICATION
				);
		assertThat(testCases).anySatisfy(test -> {
			assertThat(test.name()).isEqualTo("Happy path - POST /users");
			assertThat(test.expectedStatusCode()).isEqualTo(201);
			assertThat(test.request().body()).isInstanceOf(Map.class);
			assertThat(body(test)).containsEntry("email", "user@example.com");
		});
		assertThat(testCases).anySatisfy(test -> {
			assertThat(test.name()).isEqualTo("Missing required field: role");
			assertThat(body(test)).doesNotContainKey("role");
		});
		assertThat(testCases).anySatisfy(test -> {
			assertThat(test.name()).isEqualTo("Boundary test: age - below minimum");
			assertThat(body(test).get("age").toString()).isEqualTo("17");
		});
		assertThat(testCases).anySatisfy(test -> {
			assertThat(test.name()).isEqualTo("Invalid email: email");
			assertThat(body(test)).containsEntry("email", "not-an-email");
		});
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> body(TestCase testCase) {
		return (Map<String, Object>) testCase.request().body();
	}
}
