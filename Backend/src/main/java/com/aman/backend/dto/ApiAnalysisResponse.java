package com.aman.backend.dto;

import java.util.Map;

public record ApiAnalysisResponse (
		String title,
		String version,
		int endpointCount,
		Map<String, Integer> methods
){
}
