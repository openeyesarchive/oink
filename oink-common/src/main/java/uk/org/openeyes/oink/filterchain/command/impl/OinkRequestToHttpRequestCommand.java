/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
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
