package com.aman.backend.service;
import com.aman.backend.model.*;

import org.springframework.stereotype.Service;

@Service
public class AiContextBuilder {

	public String build(ApiEndpoint endpoint) {

		StringBuilder context = new StringBuilder();
		context.append("API ENDPOINT\n");
		context.append("====================\n");
		context.append("Method: ")
				.append(endpoint.method())
				.append("\n");

		context.append("Path: ")
				.append(endpoint.path())
				.append("\n");

		if (endpoint.summary() != null) {

			context.append("Summary: ")
					.append(endpoint.summary())
					.append("\n");
		}

		if (endpoint.description() != null) {

			context.append("Description: ")
					.append(endpoint.description())
					.append("\n");
		}

		context.append("\nPARAMETERS\n");
		context.append("====================\n");

		for (ApiParameter parameter :
				endpoint.parameters()) {

			context.append("- ")
					.append(parameter.name())
					.append("\n");

			context.append("  location: ")
					.append(parameter.location())
					.append("\n");

			context.append("  type: ")
					.append(parameter.type())
					.append("\n");

			context.append("  required: ")
					.append(parameter.required())
					.append("\n");
		}

		if (endpoint.requestBody() != null) {

			context.append("\nREQUEST BODY\n");
			context.append("====================\n");

			context.append("Content-Type: ")
					.append(
							endpoint.requestBody()
									.contentType()
					)
					.append("\n");

			context.append("Required: ")
					.append(
							endpoint.requestBody()
									.required()
					)
					.append("\n");

			for (ApiField field :
					endpoint.requestBody().fields()) {

				context.append("\nField: ")
						.append(field.name())
						.append("\n");

				context.append("  type: ")
						.append(field.type())
						.append("\n");

				context.append("  format: ")
						.append(field.format())
						.append("\n");

				context.append("  required: ")
						.append(field.required())
						.append("\n");

				if (field.minimum() != null) {

					context.append("  minimum: ")
							.append(field.minimum())
							.append("\n");
				}

				if (field.maximum() != null) {

					context.append("  maximum: ")
							.append(field.maximum())
							.append("\n");
				}

				if (field.minLength() != null) {

					context.append("  minLength: ")
							.append(field.minLength())
							.append("\n");
				}

				if (field.maxLength() != null) {

					context.append("  maxLength: ")
							.append(field.maxLength())
							.append("\n");
				}

				if (!field.enumValues().isEmpty()) {

					context.append("  enum: ")
							.append(field.enumValues())
							.append("\n");
				}
			}
		}

		context.append("\nRESPONSES\n");
		context.append("====================\n");

		for (ApiResponse response :
				endpoint.responses()) {

			context.append("- ")
					.append(response.statusCode())
					.append(": ")
					.append(response.description())
					.append("\n");
		}

		context.append("\nAUTHENTICATION\n");
		context.append("====================\n");

		if (endpoint.authentication() != null) {

			context.append("Required: ")
					.append(
							endpoint.authentication()
									.required()
					)
					.append("\n");

			context.append("Schemes: ")
					.append(
							endpoint.authentication()
									.schemes()
					)
					.append("\n");
		}

		return context.toString();
	}
}
