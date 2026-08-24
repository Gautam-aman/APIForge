package com.aman.backend.service;

import java.util.ArrayList;
import java.util.List;

import com.aman.backend.model.AiTestAgentResponse;
import com.aman.backend.model.AiTestSuggestion;
import com.aman.backend.model.AiTestValidationResult;
import com.aman.backend.model.ApiEndpoint;
import com.aman.backend.model.TestCase;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiTestAgentOrchestrator {

	private final AiTestAgentService aiTestAgentService;
	private final AiTestValidatorService validatorService;
	private final TestDuplicateDetector duplicateDetector;
	private final TestCaseGeneratorService testCaseGeneratorService;

	public List<AiTestSuggestion> generate(ApiEndpoint endpoint) {

		List<TestCase> deterministicTests = testCaseGeneratorService.generateForEndpoint(endpoint);
		AiTestAgentResponse response = aiTestAgentService.generateSuggestions(endpoint, deterministicTests);

		List<AiTestSuggestion> accepted = new ArrayList<>();

		for (AiTestSuggestion suggestion : response.suggestions()) {

			AiTestValidationResult validation = validatorService.validate(endpoint, suggestion);
			if (!validation.valid()) {
				continue;
			}

			if (duplicateDetector.isDuplicate(suggestion, deterministicTests, accepted)) {
				continue;
			}
			accepted.add(suggestion);
		}
		return accepted;
	}

}
