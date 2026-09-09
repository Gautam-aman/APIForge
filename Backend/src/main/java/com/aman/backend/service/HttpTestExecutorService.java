package com.aman.backend.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import com.aman.backend.config.ExecutionProperties;
import com.aman.backend.model.HttpResponseData;
import com.aman.backend.model.TestCase;
import com.aman.backend.model.TestRequest;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class HttpTestExecutorService {

	private final ExecutionProperties properties;
	private final ExecutionSecurityPolicy securityPolicy;
	private final ObjectMapper objectMapper;
	private final HttpClient httpClient;

	public HttpTestExecutorService(ExecutionProperties properties, ExecutionSecurityPolicy securityPolicy, ObjectMapper objectMapper) {
		this.properties = properties;
		this.securityPolicy = securityPolicy;
		this.objectMapper = objectMapper;
		this.httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
	}

	public HttpResponseData execute(String baseUrl, TestCase testCase) throws IOException, InterruptedException {
		URI baseUri = securityPolicy.validateBaseUrl(baseUrl);
		URI targetUri = buildTargetUri(baseUri, testCase.request());
		HttpRequest request = buildRequest(targetUri, testCase.request());
		long start = System.nanoTime();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
		long durationMs = Duration.ofNanos(System.nanoTime() - start).toMillis();

		return new HttpResponseData(
				response.statusCode(),
				durationMs,
				response.headers().map().entrySet().stream()
						.collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> String.join(",", entry.getValue()))),
				truncate(response.body())
		);
	}

	private URI buildTargetUri(URI baseUri, TestRequest testRequest) {
		String path = testRequest.path();
		if (testRequest.pathParameters() != null) {
			for (Map.Entry<String, Object> parameter : testRequest.pathParameters().entrySet()) {
				path = path.replace("{" + parameter.getKey() + "}", encode(String.valueOf(parameter.getValue())));
			}
		}

		String base = baseUri.toString();
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		String fullUrl = base + (path.startsWith("/") ? path : "/" + path);

		if (testRequest.queryParameters() != null && !testRequest.queryParameters().isEmpty()) {
			String query = testRequest.queryParameters().entrySet().stream()
					.map(entry -> encode(entry.getKey()) + "=" + encode(String.valueOf(entry.getValue())))
					.collect(java.util.stream.Collectors.joining("&"));
			fullUrl = fullUrl + "?" + query;
		}

		return URI.create(fullUrl);
	}

	private HttpRequest buildRequest(URI targetUri, TestRequest testRequest) {
		HttpRequest.Builder builder = HttpRequest.newBuilder(targetUri)
				.timeout(Duration.ofMillis(properties.requestTimeoutMs()));

		if (testRequest.headers() != null) {
			testRequest.headers().forEach((name, value) -> {
				if (value != null && !"host".equalsIgnoreCase(name)) {
					builder.header(name, String.valueOf(value));
				}
			});
		}

		String method = testRequest.method().toUpperCase();
		if (hasBody(method) && testRequest.body() != null) {
			builder.method(method, HttpRequest.BodyPublishers.ofString(toJson(testRequest.body())));
		} else {
			builder.method(method, HttpRequest.BodyPublishers.noBody());
		}

		return builder.build();
	}

	private boolean hasBody(String method) {
		return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method);
	}

	private String toJson(Object body) {
		try {
			return objectMapper.writeValueAsString(body);
		} catch (Exception exception) {
			throw new IllegalArgumentException("Test request body could not be serialized to JSON.");
		}
	}

	private String truncate(String body) {
		if (body == null || body.getBytes(StandardCharsets.UTF_8).length <= properties.maxResponseBodyBytes()) {
			return body;
		}
		return body.substring(0, Math.min(body.length(), properties.maxResponseBodyBytes()));
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
