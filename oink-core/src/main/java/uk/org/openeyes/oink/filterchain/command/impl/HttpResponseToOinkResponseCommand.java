/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
