package com.aman.backend.service;


import java.util.Map;

import com.aman.backend.dto.ApiAnalysisResponse;

import org.springframework.stereotype.Service;

@Service
public class ApiAnalysisService {

	public ApiAnalysisResponse analyze(String apiDefinition) {

		// Temporary implementation.

		return new ApiAnalysisResponse(
				"Demo API",
				"1.0.0",
				0,
				Map.of()
		);
	}

}
