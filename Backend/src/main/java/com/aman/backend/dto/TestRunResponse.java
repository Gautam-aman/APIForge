package com.aman.backend.dto;

import com.aman.backend.model.TestRunProgress;
import com.aman.backend.model.TestRunResult;
import com.aman.backend.model.TestRunStatus;

public record TestRunResponse(
		String runId,
		TestRunStatus status,
		TestRunProgress progress,
		TestRunResult result,
		String error
) {
}
