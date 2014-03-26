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

import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.chain.Command;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.Parser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.domain.OINKBody;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.filterchain.InvalidContextException;
import uk.org.openeyes.oink.filterchain.FilterChainContext;
import uk.org.openeyes.oink.filterchain.command.FilterCommand;

import com.google.api.client.http.HttpResponse;

@Component
public class HttpResponseToOinkResponseCommand extends FilterCommand {

	private static final Logger logger = LoggerFactory
			.getLogger(HttpResponseToOinkResponseCommand.class);

	Parser hl7JsonParser;
	Parser hl7XmlParser;

	private final static String[] jsonContentTypes = { "application/json",
			"application/json+fhir" };
	private final static String[] xmlContentTypes = { "application/xml+fhir",
			"application/atom+xml", "application/xml", "text/xml" };

	public HttpResponseToOinkResponseCommand() {
		hl7JsonParser = new JsonParser();
		hl7XmlParser = new XmlParser();
	}

	@Override
	protected boolean execute(FilterChainContext context) throws Exception {
		try {
			HttpResponse response = context.getHttpResponse();
			if (response == null) {
				throw new InvalidContextException(
						"Could not find HttpResponse in context");
			}
			OINKResponseMessage oinkResponse = convertToOINKMessage(response);
			context.setResponse(oinkResponse);
			return Command.CONTINUE_PROCESSING;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

	private OINKResponseMessage convertToOINKMessage(HttpResponse response)
			throws Exception {
		InputStream is = response.getContent();
		OINKBody b = null;

		String contentType = response.getContentType();
		if (Arrays.asList(jsonContentTypes).contains(contentType)) {
			ResourceOrFeed res = hl7JsonParser.parseGeneral(is);
			b = new OINKBody(res);
		} else if (Arrays.asList(xmlContentTypes).contains(contentType)) {
			ResourceOrFeed res = hl7XmlParser.parseGeneral(is);
			b = new OINKBody(res);
		}

		handleNullFieldValues(b); // TODO REMOVE ASAP

		OINKResponseMessage responseMessage = new OINKResponseMessage(
				response.getStatusCode(), b);
		return responseMessage;
	}

	/**
	 * Temporary workaround for a bug in the official FHIR implementation.
	 * FhirParser cannot handle field values which are null.
	 * 
	 * @param b
	 */
	public void handleNullFieldValues(OINKBody b) {
		if (b.getFeed() != null) {
			AtomFeed f = b.getFeed();
			if (f.getId() == null) {
				f.setId("NULL");
			}
			if (f.getTitle() == null) {
				f.setTitle("NULL");
			}
			for (AtomEntry<? extends Resource> e : f.getEntryList()) {
				if (e.getId() == null) {
					e.setId("NULL");
				}
				if (e.getTitle() == null) {
					e.setTitle("NULL");
				}
			}
		}
	}

}
