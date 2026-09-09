package com.aman.backend.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.aman.backend.config.ExecutionProperties;
import com.aman.backend.dto.ApiAnalysisResponse;
import com.aman.backend.dto.CreateTestRunRequest;
import com.aman.backend.dto.TestRunResponse;
import com.aman.backend.model.AssertionResult;
import com.aman.backend.model.HttpResponseData;
import com.aman.backend.model.TestCase;
import com.aman.backend.model.TestExecutionResult;
import com.aman.backend.model.TestRunProgress;
import com.aman.backend.model.TestRunResult;
import com.aman.backend.model.TestRunStatus;
import com.aman.backend.model.TestStatus;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class TestRunService {

	private final ExecutionProperties properties;
	private final ApiAnalysisService apiAnalysisService;
	private final TestCaseGeneratorService testCaseGeneratorService;
	private final HttpTestExecutorService httpTestExecutorService;
	private final AssertionService assertionService;
	private final ExecutorService executorService;
	private final ConcurrentHashMap<String, RunState> runs = new ConcurrentHashMap<>();

	public TestRunService(
			ExecutionProperties properties,
			ApiAnalysisService apiAnalysisService,
			TestCaseGeneratorService testCaseGeneratorService,
			HttpTestExecutorService httpTestExecutorService,
			AssertionService assertionService
	) {
		this.properties = properties;
		this.apiAnalysisService = apiAnalysisService;
		this.testCaseGeneratorService = testCaseGeneratorService;
		this.httpTestExecutorService = httpTestExecutorService;
		this.assertionService = assertionService;
		this.executorService = Executors.newFixedThreadPool(Math.max(1, properties.maxConcurrency()));
	}

	public TestRunResponse create(CreateTestRunRequest request) {
		List<TestCase> testCases = resolveTestCases(request);
		if (testCases.size() > properties.maxTestsPerRun()) {
			throw new IllegalArgumentException("Test run exceeds the configured maximum of " + properties.maxTestsPerRun() + " tests.");
		}

		String runId = UUID.randomUUID().toString();
		RunState state = new RunState(runId, testCases.size());
		runs.put(runId, state);

		CompletableFuture.runAsync(() -> executeRun(request.baseUrl(), testCases, state), executorService);
		return state.toResponse();
	}

	public TestRunResponse get(String runId) {
		RunState state = runs.get(runId);
		if (state == null) {
			throw new IllegalArgumentException("Test run not found: " + runId);
		}
		return state.toResponse();
	}

	private List<TestCase> resolveTestCases(CreateTestRunRequest request) {
		if (request.testCases() != null && !request.testCases().isEmpty()) {
			return request.testCases();
		}
		if (request.apiDefinition() == null || request.apiDefinition().isBlank()) {
			throw new IllegalArgumentException("Provide either testCases or apiDefinition.");
		}
		ApiAnalysisResponse analysis = apiAnalysisService.analyze(request.apiDefinition());
		return testCaseGeneratorService.generate(analysis.endpoints());
	}

	private void executeRun(String baseUrl, List<TestCase> testCases, RunState state) {
		state.markRunning();
		long start = System.nanoTime();

		try {
			for (TestCase testCase : testCases) {
				state.addResult(executeTest(baseUrl, testCase));
			}

			long durationMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
			state.complete(durationMs);
		} catch (RuntimeException exception) {
			state.fail(exception.getMessage());
		}
	}

	private TestExecutionResult executeTest(String baseUrl, TestCase testCase) {
		try {
			HttpResponseData response = httpTestExecutorService.execute(baseUrl, testCase);
			List<AssertionResult> assertions = assertionService.assertResponse(testCase, response);
			boolean passed = assertions.stream().allMatch(AssertionResult::passed);

			return new TestExecutionResult(
					testCase.id(),
					testCase.name(),
					passed ? TestStatus.PASSED : TestStatus.FAILED,
					testCase.expectedStatusCode(),
					response.statusCode(),
					response.durationMs(),
					response.body(),
					null,
					passed ? "All assertions passed." : "One or more assertions failed.",
					assertions
			);
		} catch (Exception exception) {
			return new TestExecutionResult(
					testCase.id(),
					testCase.name(),
					TestStatus.ERROR,
					testCase.expectedStatusCode(),
					null,
					0,
					null,
					exception.getMessage(),
					"Test execution failed before assertions could be evaluated.",
					List.of()
			);
		}
	}

	@PreDestroy
	void shutdown() {
		executorService.shutdownNow();
	}

	private static final class RunState {
		private final String runId;
		private final int total;
		private final AtomicInteger completed = new AtomicInteger();
		private final AtomicInteger passed = new AtomicInteger();
		private final AtomicInteger failed = new AtomicInteger();
		private final AtomicInteger errors = new AtomicInteger();
		private final List<TestExecutionResult> results = java.util.Collections.synchronizedList(new ArrayList<>());
		private volatile TestRunStatus status = TestRunStatus.QUEUED;
		private volatile TestRunResult result;
		private volatile String error;

		private RunState(String runId, int total) {
			this.runId = runId;
			this.total = total;
		}

		private void markRunning() {
			status = TestRunStatus.RUNNING;
		}

		private void addResult(TestExecutionResult executionResult) {
			results.add(executionResult);
			completed.incrementAndGet();
			if (executionResult.status() == TestStatus.PASSED) {
				passed.incrementAndGet();
			} else if (executionResult.status() == TestStatus.FAILED) {
				failed.incrementAndGet();
			} else if (executionResult.status() == TestStatus.ERROR) {
				errors.incrementAndGet();
			}
		}

		private void complete(long durationMs) {
			result = new TestRunResult(total, passed.get(), failed.get(), errors.get(), durationMs, List.copyOf(results));
			status = TestRunStatus.COMPLETED;
		}

		private void fail(String message) {
			error = message;
			result = new TestRunResult(total, passed.get(), failed.get(), errors.get(), 0, List.copyOf(results));
			status = TestRunStatus.FAILED;
		}

		private TestRunResponse toResponse() {
			return new TestRunResponse(
					runId,
					status,
					new TestRunProgress(total, completed.get(), passed.get(), failed.get(), errors.get()),
					result,
					error
			);
		}
	}
}
