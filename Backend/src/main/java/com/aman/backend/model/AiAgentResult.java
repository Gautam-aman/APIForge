package com.aman.backend.model;

import java.util.List;

public record AiAgentResult(
		List<AiTestSuggestion> suggestions,
		int generated,
		int accepted,
		int rejected,
		int duplicates
) {
}
