package com.aman.backend.controller;

import com.aman.backend.dto.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

	@GetMapping("/health")
	public ApiResponse<String> getHealth(){
		return new ApiResponse<>(
				true,
				"APIForge backend is running",
				"Health check successful"
		);
	}

}
