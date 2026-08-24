package com.aman.backend.service;

import java.util.List;

import com.aman.backend.model.AiTestSuggestion;
import com.aman.backend.model.TestCase;

import org.springframework.stereotype.Service;

@Service
public class TestDuplicateDetector {

	public boolean isDuplicate(AiTestSuggestion suggestion, List<TestCase> existingTests, List<AiTestSuggestion> acceptedSuggestions) {
		String normalized = normalize(suggestion.name());
		for (TestCase test : existingTests) {
			if (normalize(test.name()).equals(normalized)) {
				return true;
			}
		}

		for (AiTestSuggestion suggestionAlreadyAccepted : acceptedSuggestions) {
			if (normalize(suggestionAlreadyAccepted.name()).equals(normalized)) {
				return true;
			}
		}
		return false;
	}

	private String normalize(String value) {
		if (value == null) {
			return "";
		}
		return value.toLowerCase().replaceAll("[^a-z0-9]", "");
	}
}
