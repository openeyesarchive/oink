package uk.org.openeyes.oink.http;

import java.io.InputStream;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
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
 * Builds an {@link OINKRequestMessage} from an Http Inbound Component.
 * 
 */
public class OinkHttpConverter {

	private static final Logger logger = LoggerFactory
			.getLogger(OinkHttpConverter.class);

	public OINKRequestMessage buildOinkRequest(@Body InputStream body,
			@Header(Exchange.CONTENT_TYPE) String mimeType,
			@Header(Exchange.CONTENT_LENGTH) int length,
			@Header(Exchange.AUTHENTICATION) Subject subject,
			@Header(Exchange.HTTP_PATH) String path,
			@Header(Exchange.HTTP_METHOD) String verb,
			@Header(Exchange.HTTP_QUERY) String query)
			throws InvalidFhirRequestException {

		OINKRequestMessage message = new OINKRequestMessage();

		// Wrap FHIR Request
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		message.setResourcePath(path);

		HttpMethod method = HttpMethod.valueOf(verb);
		message.setMethod(method);

		message.setParameters(query);

		if (length > 0) {
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

	public static FhirBody readFhirBody(InputStream is) throws InvalidFhirRequestException {
		
		JsonParser parser = new JsonParser();
		try {
			ResourceOrFeed res = parser.parseGeneral(is);
			if (res.getFeed() != null) {
				return new FhirBody(res.getFeed());
			} else {
				return new FhirBody(res.getResource());
			}
		} catch (Exception e) {
			throw new InvalidFhirRequestException("Could not read Fhir Body. Details: "
					+ e.getMessage());
		}


	}

	public String buildHttpResponse(@Body OINKResponseMessage message,
			@OutHeaders Map<String, Object> headers) throws InvalidFhirResponseException {
		headers.put(Exchange.HTTP_RESPONSE_CODE, message.getStatus());
		headers.put(Exchange.CONTENT_TYPE, "application/json+fhir");
		
		try {
		FhirBody body = message.getBody();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonComposer composer = new JsonComposer();
		if (body.isResource()) {
			composer.compose(os, body.getResource(), false);
		} else {
			composer.compose(os, body.getBundle(), false);
		}
		return os.toString();
		} catch (Exception e) {
			throw new InvalidFhirResponseException(e.getMessage());
		}
	}

}
