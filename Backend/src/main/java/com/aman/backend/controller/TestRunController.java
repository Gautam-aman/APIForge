package com.aman.backend.controller;

import com.aman.backend.dto.ApiResponse;
import com.aman.backend.dto.CreateTestRunRequest;
import com.aman.backend.dto.TestRunResponse;
import com.aman.backend.service.TestRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-runs")
@RequiredArgsConstructor
public class TestRunController {

	private final TestRunService testRunService;

	@PostMapping
	public ApiResponse<TestRunResponse> create(@Valid @RequestBody CreateTestRunRequest request) {
		return new ApiResponse<>(testRunService.create(request), "Test run queued successfully");
	}

	@GetMapping("/{runId}")
	public ApiResponse<TestRunResponse> get(@PathVariable String runId) {
		return new ApiResponse<>(testRunService.get(runId), "Test run loaded successfully");
	}
}
