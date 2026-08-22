package com.aman.backend.dto;

public record ApiResponse<T>(
		boolean success,
		T data,
		String message
) {

}
