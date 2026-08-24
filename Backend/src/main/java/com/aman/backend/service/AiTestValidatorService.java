package com.aman.backend.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.aman.backend.model.AiTestSuggestion;
import com.aman.backend.model.AiTestValidationResult;
import com.aman.backend.model.ApiEndpoint;
import com.aman.backend.model.ApiField;
import com.aman.backend.model.ApiParameter;

import org.springframework.stereotype.Service;

@Service
public class AiTestValidatorService {

	public AiTestValidationResult validate(ApiEndpoint endpoint, AiTestSuggestion suggestion) {

		if (suggestion.name() == null || suggestion.name().isBlank()) {
			return invalid("Test name cannot be empty.");
		}
		if (suggestion.category() == null) {
			return invalid("Test category cannot be null.");
		}
		if (suggestion.priority() == null) {
			return invalid("Test priority cannot be null.");
		}
		if (suggestion.expectedStatusCode() == null) {
			return invalid("Expected status code cannot be null.");
		}

		if (suggestion.expectedStatusCode() < 100 || suggestion.expectedStatusCode() > 599) {
			return invalid("Expected status code must be between 100 and 599.");
		}

		AiTestValidationResult bodyResult = validateBody(endpoint, suggestion.body());
		if (!bodyResult.valid()) {
			return bodyResult;
		}

		AiTestValidationResult pathResult = validatePathParameters(endpoint, suggestion.pathParameters());
		if (!pathResult.valid()) {return pathResult;}
		return new AiTestValidationResult(true, "Valid");
	}

	private AiTestValidationResult validateBody(ApiEndpoint endpoint, Object body) {
		if (endpoint.requestBody() == null) {
			if (body != null) {
				return invalid("Endpoint does not define a request body.");
			}
			return valid();
		}
		if (!(body instanceof Map<?, ?>)) {
			return invalid("Request body must be a JSON object.");
		}

		Map<?, ?> bodyMap = (Map<?, ?>) body;

		Set<String> allowedFields = new HashSet<>();

		for (ApiField field : endpoint.requestBody().fields()) {
			allowedFields.add(field.name());
		}

		for (Object key : bodyMap.keySet()) {
			String fieldName = String.valueOf(key);
			if (!allowedFields.contains(fieldName)) {
				return invalid(
						"Field '" +
								fieldName +
								"' does not exist in the API schema."
				);
			}
		}
		return valid();
	}

	private AiTestValidationResult validatePathParameters(ApiEndpoint endpoint, Map<String, Object> pathParameters) {
		if (pathParameters == null) {
			return valid();
		}
		for (ApiParameter parameter : endpoint.parameters()) {
			if (!"path".equals(parameter.location())) {
				continue;
			}
			if (!parameter.required()) {
				continue;
			}
			if (!pathParameters.containsKey(parameter.name())) {
				return invalid("Missing required path parameter: " + parameter.name()
				);
			}
		}
		return valid();
	}

	private AiTestValidationResult valid() {
		return new AiTestValidationResult(true, "Valid");
	}

	private AiTestValidationResult invalid(String reason) {
		return new AiTestValidationResult(false, reason
		);
	}

}
