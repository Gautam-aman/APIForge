package com.aman.backend.controller;


import java.util.List;

import com.aman.backend.dto.AnalyzeApiRequest;
import com.aman.backend.dto.ApiAnalysisResponse;
import com.aman.backend.dto.ApiResponse;
import com.aman.backend.model.TestCase;
import com.aman.backend.service.ApiAnalysisService;
import com.aman.backend.service.TestCaseGeneratorService;
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
	private final TestCaseGeneratorService testCaseGeneratorService;

	@PostMapping("/analyze")
	public ApiResponse<ApiAnalysisResponse> analyze(@Valid @RequestBody AnalyzeApiRequest request) {
		ApiAnalysisResponse response = apiAnalysisService.analyze(request.apiDefinition());

		return new ApiResponse<>(response, "API analyzed successfully");
	}

	@PostMapping("/test-cases")
	public ApiResponse<List<TestCase>> generateTestCases(@Valid @RequestBody AnalyzeApiRequest request) {

		ApiAnalysisResponse analysis = apiAnalysisService.analyze(request.apiDefinition());

		List<TestCase> testCases = testCaseGeneratorService.generate(analysis.endpoints());

		return new ApiResponse<>(
				testCases,
				"Test cases generated successfully"
		);
	}
}
