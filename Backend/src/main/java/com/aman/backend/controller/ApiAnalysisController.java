package com.aman.backend.controller;


import com.aman.backend.dto.AnalyzeApiRequest;
import com.aman.backend.dto.ApiAnalysisResponse;
import com.aman.backend.dto.ApiResponse;
import com.aman.backend.service.ApiAnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/apis")
@RequiredArgsConstructor
public class ApiAnalysisController {

	private final ApiAnalysisService apiAnalysisService;

	@PostMapping("/analyze")
	public ApiResponse<ApiAnalysisResponse> analyze(@Valid @RequestBody AnalyzeApiRequest request) {
		ApiAnalysisResponse response = apiAnalysisService.analyze(request.apiDefinition());

		return new ApiResponse<>(true, response, "API analyzed successfully");
	}

}
