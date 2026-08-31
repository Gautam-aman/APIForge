package com.aman.backend.service;

import com.aman.backend.model.ApiEndpoint;
import com.aman.backend.model.ApiField;
import com.aman.backend.model.ApiParameter;
import com.aman.backend.model.ApiRequestBody;
import com.aman.backend.model.ApiResponse;
import com.aman.backend.model.AuthenticationInfo;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpenApiParserService {

	public OpenAPI parse(String apiDefinition) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(apiDefinition, null, null);
		if (result.getOpenAPI() == null) {
			String errors = String.join(", ", result.getMessages());
			throw new IllegalArgumentException("Invalid OpenAPI specification: " + errors);
		}
		return result.getOpenAPI();
	}

	public List<ApiEndpoint> extractEndpoints(OpenAPI openAPI) {
		List<ApiEndpoint> endpoints = new ArrayList<>();

		if (openAPI.getPaths() == null) {
			return endpoints;
		}

		for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
			String path = entry.getKey();
			PathItem pathItem = entry.getValue();

			addEndpoint(endpoints, openAPI, path, "GET", pathItem.getGet());
			addEndpoint(endpoints, openAPI, path, "POST", pathItem.getPost());
			addEndpoint(endpoints, openAPI, path, "PUT", pathItem.getPut());
			addEndpoint(endpoints, openAPI, path, "DELETE", pathItem.getDelete());
			addEndpoint(endpoints, openAPI, path, "PATCH", pathItem.getPatch());
			addEndpoint(endpoints, openAPI, path, "HEAD", pathItem.getHead());
			addEndpoint(endpoints, openAPI, path, "OPTIONS", pathItem.getOptions());
		}

		return endpoints;
	}

	private void addEndpoint(List<ApiEndpoint> endpoints, OpenAPI openAPI, String path, String method, Operation operation) {
		if (operation == null) {
			return;
		}

		List<ApiParameter> parameters = extractParameters(operation);
		ApiRequestBody requestBody = extractRequestBody(operation, openAPI);
		List<com.aman.backend.model.ApiResponse> responses = extractResponses(operation);
		AuthenticationInfo authentication = extractAuthentication(openAPI, operation);

		endpoints.add(new ApiEndpoint(
				path,
				method,
				operation.getSummary(),
				operation.getDescription(),
				parameters,
				requestBody,
				responses,
				authentication
		));
	}

	private List<ApiParameter> extractParameters(Operation operation) {
		List<ApiParameter> parameters = new ArrayList<>();

		if (operation.getParameters() == null) {
			return parameters;
		}

		for (Parameter parameter : operation.getParameters()) {
			Schema<?> schema = parameter.getSchema();
			String type = schema != null ? schema.getType() : null;
			String format = schema != null ? schema.getFormat() : null;

			parameters.add(new ApiParameter(
					parameter.getName(),
					parameter.getIn(),
					Boolean.TRUE.equals(parameter.getRequired()),
					type,
					format
			));
		}

		return parameters;
	}

	private ApiRequestBody extractRequestBody(Operation operation, OpenAPI openAPI) {
		if (operation.getRequestBody() == null) {
			return null;
		}

		boolean required = Boolean.TRUE.equals(operation.getRequestBody().getRequired());
		Content content = operation.getRequestBody().getContent();

		if (content == null || content.isEmpty()) {
			return new ApiRequestBody(required, null, List.of());
		}

		Map.Entry<String, MediaType> entry = content.entrySet().iterator().next();
		String contentType = entry.getKey();
		MediaType mediaType = entry.getValue();
		Schema<?> schema = mediaType.getSchema();

		List<ApiField> fields = extractFields(schema, openAPI);

		return new ApiRequestBody(required, contentType, fields);
	}

	private List<ApiField> extractFields(Schema<?> schema, OpenAPI openAPI) {
		if (schema == null) {
			return List.of();
		}

		if (schema.get$ref() != null) {
			schema = resolveSchema(schema.get$ref(), openAPI);
		}

		if (schema == null || schema.getProperties() == null) {
			return List.of();
		}

		List<ApiField> fields = new ArrayList<>();

		for (Map.Entry<String, Schema> entry : schema.getProperties().entrySet()) {
			String fieldName = entry.getKey();
			Schema fieldSchema = entry.getValue();

			boolean required = schema.getRequired() != null && schema.getRequired().contains(fieldName);

			List<String> enumValues = fieldSchema.getEnum() == null
					? List.of()
					: fieldSchema.getEnum().stream().map(Object::toString).toList();

			fields.add(new ApiField(
					fieldName,
					fieldSchema.getType(),
					fieldSchema.getFormat(),
					required,
					fieldSchema.getDescription(),
					fieldSchema.getExample(),
					enumValues,
					fieldSchema.getMinimum(),
					fieldSchema.getMaximum(),
					fieldSchema.getMinLength(),
					fieldSchema.getMaxLength(),
					fieldSchema.getPattern()
			));
		}

		return fields;
	}

	private Schema<?> resolveSchema(String reference, OpenAPI openAPI) {
		if (reference == null) {
			return null;
		}

		String schemaName = reference.substring(reference.lastIndexOf("/") + 1);
		Components components = openAPI.getComponents();

		if (components == null || components.getSchemas() == null) {
			return null;
		}

		return components.getSchemas().get(schemaName);
	}

	private List<ApiResponse> extractResponses(Operation operation) {
		List<ApiResponse> responses = new ArrayList<>();

		if (operation.getResponses() == null) {
			return responses;
		}

		operation.getResponses().forEach((statusCode, response) ->
				responses.add(new ApiResponse(statusCode, response.getDescription()))
		);

		return responses;
	}

	private AuthenticationInfo extractAuthentication(OpenAPI openAPI, Operation operation) {
		List<String> schemes = new ArrayList<>();

		if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
			for (SecurityRequirement requirement : operation.getSecurity()) {
				schemes.addAll(requirement.keySet());
			}
		}

		if (schemes.isEmpty() && openAPI.getSecurity() != null) {
			for (SecurityRequirement requirement : openAPI.getSecurity()) {
				schemes.addAll(requirement.keySet());
			}
		}

		return new AuthenticationInfo(!schemes.isEmpty(), schemes);
	}
}
