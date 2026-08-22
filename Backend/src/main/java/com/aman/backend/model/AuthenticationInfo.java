package com.aman.backend.model;

import java.util.List;

public record AuthenticationInfo(
		boolean required,
		List<String> schemes
) {
}
