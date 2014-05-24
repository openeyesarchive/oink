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
package uk.org.openeyes.oink.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Headers;
import org.apache.camel.OutHeaders;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.hl7.fhir.instance.model.AtomCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

/**
 * Builds an {@link OINKRequestMessage} from an Http Consumer (i.e. Camel Jetty
 * Component). It also has methods for converting the contents of an
 * {@link OINKResponseMessage} into the format able to be handled by an Http
 * Processor.
 * 
 */
public class OinkHttpConverter {

	private static final Logger logger = LoggerFactory
			.getLogger(OinkHttpConverter.class);

	public OINKResponseMessage buildOinkResponse(
			@Headers Map<String, Object> headers, @Body String body)
			throws InvalidFhirRequestException, IOException {

		OINKResponseMessage response = new OINKResponseMessage();

		if (body != null) {
			try {
				InputStream is = new ByteArrayInputStream(body.getBytes());
				FhirBody fhirBody = readFhirBody(is);
				response.setBody(fhirBody);
			} catch (InvalidFhirRequestException ex) {
				logger.warn("The response body is not a Fhir Resource or Bundle");
			}
		}
		if (headers.containsKey(Exchange.HTTP_RESPONSE_CODE)) {
			int code = (int) headers.get(Exchange.HTTP_RESPONSE_CODE);
			response.setStatus(code);
		}
		if (headers.containsKey("Location")) {
			response.setLocationHeader((String) headers.get("Location"));
		}
		return response;
	}

	public OINKRequestMessage buildOinkRequest(
			@Headers Map<String, Object> headers, @Body InputStream body)
			throws InvalidFhirRequestException, IOException {

		String mimeType = (String) headers.get(Exchange.CONTENT_TYPE);
		Integer length = headers.containsKey(Exchange.CONTENT_LENGTH) ? Integer
				.parseInt((String) headers.get(Exchange.CONTENT_LENGTH)) : null;
		String path = (String) headers.get(Exchange.HTTP_PATH);
		String verb = (String) headers.get(Exchange.HTTP_METHOD);
		String query = (String) headers.get(Exchange.HTTP_QUERY);

		OINKRequestMessage message = new OINKRequestMessage();

		// Wrap FHIR Request
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		message.setResourcePath(path);

		HttpMethod method = HttpMethod.valueOf(verb);
		message.setMethod(method);

		message.setParameters(query);

		if (length != null && length > 0) {
			if (mimeType == null) {
				logger.error("Could parse message body as FhirBody because the Content-Type header was missing");
				throw new InvalidFhirRequestException(
						"Could not parse Fhir Body. Content-Type header should be set");
			}
			if (!mimeType.equals("application/json+fhir")) {
				logger.error("Could parse message body as FhirBody because the Content-Type:"
						+ mimeType + " is not supported");
				throw new InvalidFhirRequestException(
						"Could not parse Fhir Body. Content-Type should be application/json+fhir");
			}
			FhirBody fhirBody = readFhirBody(body);
			message.setBody(fhirBody);
		}

		return message;
	}

	public static FhirBody readFhirBody(InputStream is)
			throws InvalidFhirRequestException, IOException {

		if (is.available() <= 0) {
			return null;
		}

		JsonParser parser = new JsonParser();
		try {
			ResourceOrFeed res = parser.parseGeneral(is);
			if (res.getFeed() != null) {
				return new FhirBody(res.getFeed());
			} else {
				return new FhirBody(res.getResource());
			}
		} catch (Exception e) {
			throw new InvalidFhirRequestException(
					"Could not read Fhir Body. Details: " + e.getMessage());
		}

	}

	public String buildHttpRequest(@Body OINKRequestMessage message,
			Exchange exchange) throws InvalidFhirRequestException {

		Map<String, Object> headers = exchange.getIn().getHeaders();
		headers.put(Exchange.HTTP_PATH, message.getResourcePath());
		headers.put(Exchange.HTTP_QUERY, message.getParameters());
		headers.put(Exchange.HTTP_CHARACTER_ENCODING, "UTF-8");
		headers.put(Exchange.HTTP_METHOD, message.getMethod().toString());

		List<AtomCategory> tags = message.getTags();
		if (!tags.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			Iterator<AtomCategory> it = tags.iterator();
			while (it.hasNext()) {
				AtomCategory tag = it.next();
				sb.append(tag.getTerm());
				sb.append("; scheme=");
				sb.append('"');
				sb.append(tag.getScheme());
				sb.append('"');
				sb.append("; label=");
				sb.append('"');
				sb.append(tag.getLabel());
				sb.append('"');
				if (it.hasNext()) {
					sb.append(", ");
				}
			}
			headers.put("Category", sb.toString());
		}

		headers.put("Accept", "application/json+fhir; charset=UTF-8");
		try {
			FhirBody body = message.getBody();
			if (body != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				JsonComposer composer = new JsonComposer();
				if (body.isResource()) {
					composer.compose(os, body.getResource(), false);
				} else {
					composer.compose(os, body.getBundle(), false);
				}
				headers.put(Exchange.CONTENT_TYPE,
						"application/json+fhir; charset=UTF-8");
				headers.put(Exchange.CHARSET_NAME, "UTF-8");
				String bodyStr = os.toString("UTF-8");
				return bodyStr;
			}
		} catch (Exception e) {
			throw new InvalidFhirRequestException(e.getMessage());
		}
		return null;
	}

	public String buildHttpResponse(@Body OINKResponseMessage message,
			@OutHeaders Map<String, Object> headers)
			throws InvalidFhirResponseException {
		headers.put(Exchange.HTTP_RESPONSE_CODE, message.getStatus());

		try {
			FhirBody body = message.getBody();
			if (body != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				JsonComposer composer = new JsonComposer();
				if (body.isResource()) {
					composer.compose(os, body.getResource(), false);
				} else {
					composer.compose(os, body.getBundle(), false);
				}
				headers.put(Exchange.CONTENT_TYPE, "application/json+fhir");
				return os.toString();
			}
		} catch (Exception e) {
			throw new InvalidFhirResponseException(e.getMessage());
		}
		return "";
	}

}
