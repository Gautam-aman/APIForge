package com.aman.backend.model;

import java.math.BigDecimal;
import java.util.List;

public record ApiField(
		String name,
		String type,
		String format,
		boolean required,
		String description,
		Object example,
		List<String> enumValues,
		BigDecimal minimum,
		BigDecimal maximum,
		Integer minLength,
		Integer maxLength,
		String pattern

) {
}