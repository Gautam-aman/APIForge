package com.aman.backend.model;

import java.util.List;

public record ApiField(
		String name,
		String type,
		String format,
		boolean required,
		String description,
		Object example,
		List<String> enumValues
) {
}