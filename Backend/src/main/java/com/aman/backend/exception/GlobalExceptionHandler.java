package com.aman.backend.exception;

import com.aman.backend.dto.ApiResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException exception) {
		ApiResponse<Void> response =
				new ApiResponse<>(
						false,
						null,
						exception.getMessage()
				);

		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(response);
	}
}
