package uk.org.openeyes.oink.filterchain.command.impl;

import java.io.IOException;

import org.apache.commons.chain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.filterchain.InvalidContextException;
import uk.org.openeyes.oink.filterchain.FilterChainContext;
import uk.org.openeyes.oink.filterchain.command.FilterCommand;

/*
 * Creates a partially-complete HttpRequest with the Context, Headers and Method of the OinkRequestMessage. Note that the URL is not set.
 */
@Component
public class OinkRequestToHttpRequestCommand extends FilterCommand {

	private final HttpTransport t = new NetHttpTransport();
	
	private final static Logger logger = LoggerFactory.getLogger(OinkRequestToHttpRequestCommand.class);

	@Override
	protected boolean execute(FilterChainContext context) throws Exception {
		try {
			OINKRequestMessage request = context.getRequest();
			if (request == null) {
				throw new InvalidContextException(
						"Could not find OINKRequestMessage in context");
			}
			HttpRequest httpRequest = unwrapHttpRequest(request);
			context.setHttpRequest(httpRequest);
			return Command.CONTINUE_PROCESSING;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	private HttpRequest unwrapHttpRequest(OINKRequestMessage message)
			throws IOException {
		HttpRequestFactory factory = t.createRequestFactory();

		// Method
		String method = message.getMethod().toString();

		// Set Content
		HttpContent content;
		if (message.getBody() != null) {
			// Write body to JSON
			ObjectMapper mapper = new ObjectMapper();
			byte[] jsonBody = mapper.writeValueAsBytes(message.getBody());
			content = new ByteArrayContent("application/json+fhir", jsonBody);
		} else {
			content = new EmptyContent();
		}

		HttpRequest httpRequest = factory.buildRequest(method, null, content);

		return httpRequest;
	}

}
