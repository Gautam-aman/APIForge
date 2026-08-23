package com.aman.backend.model;

import java.util.List;

public record AiTestAgentResponse (
	List<AiTestSuggestion> suggestions){
}
