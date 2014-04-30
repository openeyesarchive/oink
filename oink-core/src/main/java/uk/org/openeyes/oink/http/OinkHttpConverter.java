package uk.org.openeyes.oink.http;

import java.io.InputStream;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.Headers;
import org.apache.camel.OutHeaders;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
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

	public OINKRequestMessage buildOinkRequest(
			@Headers Map<String, Object> headers, @Body InputStream body)
			throws InvalidFhirRequestException {

		String mimeType = (String) headers.get(Exchange.CONTENT_TYPE);
		Integer length = headers.containsKey(Exchange.CONTENT_LENGTH) ? Integer.parseInt((String) headers.get(Exchange.CONTENT_LENGTH)) : null;
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
			throws InvalidFhirRequestException {

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
		return null;
	}

}
