package com.aman.backend.service;

import java.util.ArrayList;
import java.util.List;

import com.aman.backend.model.AssertionResult;
import com.aman.backend.model.AssertionType;
import com.aman.backend.model.HttpResponseData;
import com.aman.backend.model.TestCase;
import org.springframework.stereotype.Service;

@Service
public class AssertionService {

	public List<AssertionResult> assertResponse(TestCase testCase, HttpResponseData response) {
		List<AssertionResult> assertions = new ArrayList<>();
		Integer expectedStatus = testCase.expectedStatusCode();
		int actualStatus = response.statusCode();

		assertions.add(new AssertionResult(
				expectedStatus != null && expectedStatus == actualStatus,
				AssertionType.STATUS_CODE,
				String.valueOf(expectedStatus),
				String.valueOf(actualStatus),
				expectedStatus != null && expectedStatus == actualStatus
						? "Expected status code was returned."
						: "Expected HTTP " + expectedStatus + " but received HTTP " + actualStatus + "."
		));

		if (expectedStatus != null && expectedStatus >= 200 && expectedStatus < 300) {
			boolean bodyPresent = response.body() != null && !response.body().isBlank();
			assertions.add(new AssertionResult(
					bodyPresent,
					AssertionType.RESPONSE_BODY_PRESENT,
					"non-empty response body",
					bodyPresent ? "present" : "empty",
					bodyPresent ? "Response body is present." : "Successful response did not include a response body."
			));
		}

		return assertions;
	}
}
