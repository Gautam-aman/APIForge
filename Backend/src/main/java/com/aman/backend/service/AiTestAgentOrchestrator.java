package com.aman.backend.service;

import java.util.ArrayList;
import java.util.List;

import com.aman.backend.model.AiAgentResult;
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

	public AiAgentResult generate(ApiEndpoint endpoint) {
		List<TestCase> deterministicTests = testCaseGeneratorService.generateForEndpoint(endpoint);

		AiTestAgentResponse response = aiTestAgentService.generateSuggestions(endpoint, deterministicTests);

		List<AiTestSuggestion> accepted = new ArrayList<>();
		int rejected = 0;
		int duplicates = 0;

		for (AiTestSuggestion suggestion : response.suggestions()) {

			AiTestValidationResult validation =
					validatorService.validate(
							endpoint,
							suggestion
					);

			if (!validation.valid()) {
				rejected++;
				continue;
			}

			if (duplicateDetector.isDuplicate(suggestion, deterministicTests, accepted)) {
				duplicates++;
				continue;
			}
			accepted.add(suggestion);
		}

		return new AiAgentResult(
				accepted,
				response.suggestions().size(),
				accepted.size(),
				rejected,
				duplicates
		);
	}

}
