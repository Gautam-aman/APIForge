package com.aman.backend.dto;

import java.util.*;

import com.aman.backend.model.ApiEndpoint;

public record ApiAnalysisResponse (
		String title,
		String version,
		int endpointCount,
		Map<String, Integer> methods,
		List<ApiEndpoint> endpoints
){
}
