package com.aman.backend.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aman.backend.dto.ApiAnalysisResponse;
import com.aman.backend.model.ApiEndpoint;
import io.swagger.v3.oas.models.OpenAPI;

import org.springframework.stereotype.Service;

@Service
public class ApiAnalysisService {
	private final OpenApiParserService parserService;

	public ApiAnalysisService(OpenApiParserService parserService) {
		this.parserService = parserService;
	}

	public ApiAnalysisResponse analyze(String apiDefinition) {

		OpenAPI openAPI = parserService.parse(apiDefinition);
		List<ApiEndpoint> endpoints = parserService.extractEndpoints(openAPI);
		Map<String, Integer> methods = new HashMap<>();

		for (ApiEndpoint endpoint : endpoints) {
			methods.merge(endpoint.method(), 1, Integer::sum);
		}

		String title = null;
		if (openAPI.getInfo() != null) {
			title = openAPI.getInfo().getTitle();
		}

		String version = null;

		if (openAPI.getInfo() != null) {
			version = openAPI.getInfo().getVersion();
		}

		return new ApiAnalysisResponse(
				title,
				version,
				endpoints.size(),
				methods,
				endpoints
		);
	}
}
