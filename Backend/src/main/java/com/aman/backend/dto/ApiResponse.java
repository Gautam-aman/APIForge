package com.aman.backend.dto;

public record ApiResponse<T>(
		T data,
		String message
) {

}
