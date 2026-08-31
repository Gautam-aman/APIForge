package com.aman.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.aman.backend.model.ApiEndpoint;
import com.aman.backend.model.ApiField;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

class OpenApiParserServiceTests {

	private final OpenApiParserService parserService = new OpenApiParserService();

	@Test
	void extractsRequestBodyFieldsFromReferencedSchema() {
		OpenAPI openAPI = parserService.parse(sampleOpenApi());

		List<ApiEndpoint> endpoints = parserService.extractEndpoints(openAPI);

		assertThat(endpoints).hasSize(1);
		ApiEndpoint endpoint = endpoints.getFirst();
		assertThat(endpoint.path()).isEqualTo("/users");
		assertThat(endpoint.method()).isEqualTo("POST");
		assertThat(endpoint.authentication().required()).isTrue();
		assertThat(endpoint.authentication().schemes()).containsExactly("bearerAuth");
		assertThat(endpoint.requestBody().contentType()).isEqualTo("application/json");
		assertThat(endpoint.requestBody().fields()).extracting(ApiField::name)
				.containsExactlyInAnyOrder("email", "age", "role");

		ApiField email = field(endpoint, "email");
		assertThat(email.required()).isTrue();
		assertThat(email.type()).isEqualTo("string");
		assertThat(email.format()).isEqualTo("email");
		assertThat(email.minLength()).isEqualTo(6);
		assertThat(email.maxLength()).isEqualTo(64);

		ApiField age = field(endpoint, "age");
		assertThat(age.minimum()).isEqualByComparingTo(BigDecimal.valueOf(18));
		assertThat(age.maximum()).isEqualByComparingTo(BigDecimal.valueOf(99));

		ApiField role = field(endpoint, "role");
		assertThat(role.enumValues()).containsExactly("USER", "ADMIN");
	}

	private ApiField field(ApiEndpoint endpoint, String name) {
		return endpoint.requestBody().fields().stream()
				.filter(field -> field.name().equals(name))
				.findFirst()
				.orElseThrow();
	}

	static String sampleOpenApi() {
		return """
				openapi: 3.0.3
				info:
				  title: Users API
				  version: 1.0.0
				security:
				  - bearerAuth: []
				components:
				  securitySchemes:
				    bearerAuth:
				      type: http
				      scheme: bearer
				  schemas:
				    CreateUserRequest:
				      type: object
				      required:
				        - email
				        - role
				      properties:
				        email:
				          type: string
				          format: email
				          minLength: 6
				          maxLength: 64
				        age:
				          type: integer
				          minimum: 18
				          maximum: 99
				        role:
				          type: string
				          enum:
				            - USER
				            - ADMIN
				paths:
				  /users:
				    post:
				      summary: Create user
				      requestBody:
				        required: true
				        content:
				          application/json:
				            schema:
				              $ref: '#/components/schemas/CreateUserRequest'
				      responses:
				        '201':
				          description: Created
				        '400':
				          description: Invalid request
				""";
	}
}
