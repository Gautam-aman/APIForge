package com.aman.backend.service;

import java.util.List;

import com.aman.backend.model.AiTestAgentResponse;
import com.aman.backend.model.ApiEndpoint;
import com.aman.backend.model.TestCase;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiTestAgentService {

	private final ChatClient chatClient;
	private final AiContextBuilder aiContextBuilder;

	public AiTestAgentService(ChatClient.Builder chatClientBuilder , AiContextBuilder aiContextBuilder) {
		this.aiContextBuilder = aiContextBuilder;
		this.chatClient = chatClientBuilder.build();
	}

	public AiTestAgentResponse generateSuggestions(ApiEndpoint endpoint, List<TestCase> existingTests) {
		String prompt = buildPrompt(endpoint, existingTests);

		return chatClient
				.prompt()
				.system(systemPrompt())
				.user(prompt)
				.call()
				.entity(AiTestAgentResponse.class);
	}

	private String systemPrompt() {
		return """
                You are APIForge's API testing expert.

                Your job is to analyze a structured API endpoint
                and identify additional high-value API test cases.

                Rules:

                1. Do not invent API behavior that has no evidence.
                2. Use the endpoint schema, parameters,
                   authentication and responses as evidence.
                3. Do not duplicate existing test cases.
                4. Focus on realistic edge cases.
                5. Consider validation, security,
                   authentication, authorization,
                   idempotency, concurrency and error handling
                   when relevant.
                6. Explain why each suggestion is useful.
                7. Return only structured test suggestions.
                """;
	}

	private String buildPrompt(ApiEndpoint endpoint, List<TestCase> existingTests) {
		StringBuilder prompt = new StringBuilder();

		prompt.append("""
                Analyze this API endpoint.

                Endpoint:
                """);
		prompt.append(aiContextBuilder.build(endpoint));
		prompt.append("""
                Existing deterministic tests:
                """);

		for (TestCase test : existingTests) {
			prompt.append("\n- ")
					.append(test.name())
					.append(" [")
					.append(test.category())
					.append("]");
		}
		prompt.append("""

                Generate additional tests that provide
                meaningful coverage beyond the existing tests.
                Return at most 8 suggestions.
                """);

		return prompt.toString();
	}
}
