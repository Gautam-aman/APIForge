package com.aman.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aman.backend.model.ApiEndpoint;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import org.springframework.stereotype.Service;

@Service
public class OpenApiParserService {

	public OpenAPI parse(String apiDefinition) {
		SwaggerParseResult result = new OpenAPIV3Parser().readContents(apiDefinition, null, null);
		if (result.getOpenAPI() == null) {
			String errors = String.join(", ", result.getMessages());
			throw new IllegalArgumentException(
					"Invalid OpenAPI specification: " + errors
			);
		}
		return result.getOpenAPI();
	}

	public List<ApiEndpoint> extractEndpoints(OpenAPI openAPI) {
		List<ApiEndpoint> endpoints = new ArrayList<>();

		if (openAPI.getPaths() == null) {return endpoints;}

		for (Map.Entry<String, PathItem> entry : openAPI.getPaths().entrySet()) {
			String path = entry.getKey();
			PathItem pathItem = entry.getValue();
			addEndpoint(endpoints, path, "GET", pathItem.getGet());
			addEndpoint(endpoints, path, "POST", pathItem.getPost());
			addEndpoint(endpoints, path, "PUT", pathItem.getPut());
			addEndpoint(endpoints, path, "DELETE", pathItem.getDelete());
			addEndpoint(endpoints, path, "PATCH", pathItem.getPatch());
			addEndpoint(endpoints, path, "HEAD", pathItem.getHead());
			addEndpoint(endpoints, path, "OPTIONS", pathItem.getOptions());
		}
		return endpoints;
	}

	private void addEndpoint(List<ApiEndpoint> endpoints, String path, String method, Operation operation) {
		if (operation == null) {return;}
		List<String> responseCodes = operation.getResponses().keySet().stream().toList();

		endpoints.add(new ApiEndpoint(
						path,
						method,
						operation.getSummary(),
						responseCodes
				)
		);
	}

}
