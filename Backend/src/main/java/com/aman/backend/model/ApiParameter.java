package com.aman.backend.model;

public record ApiParameter(
		String name,
		String location,
		boolean required,
		String type,
		String format
) {
}
