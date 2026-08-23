package com.aman.backend.dto;

import java.util.List;

import com.aman.backend.model.TestCase;

public record ApiResponse<T>(
		T data,
		String message
) {
}
