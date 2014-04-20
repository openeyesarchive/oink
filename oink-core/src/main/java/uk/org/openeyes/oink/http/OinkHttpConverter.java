package uk.org.openeyes.oink.http;

import java.util.Map;

import javax.security.auth.Subject;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.apache.camel.OutHeaders;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

/**
 * Builds an {@link OINKRequestMessage} from an Http Inbound Component.
 * 
 */
public class OinkHttpConverter {

	public OINKRequestMessage buildOinkRequest(@Body FhirBody body,
			@Header(Exchange.AUTHENTICATION) Subject subject,
			@Header(Exchange.HTTP_PATH) String path,
			@Header(Exchange.HTTP_METHOD) String verb,
			@Header(Exchange.HTTP_QUERY) String query) {
		
		OINKRequestMessage message = new OINKRequestMessage();
		
		// Wrap FHIR Request
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		message.setResourcePath(path);

		HttpMethod method = HttpMethod.valueOf(verb);
		message.setMethod(method);

		message.setParameters(query);

		message.setBody(body);
		
		return message;
	}
	
	public FhirBody buildHttpResponse(@Body OINKResponseMessage message, @OutHeaders Map<String, Object> headers) {
		headers.put(Exchange.HTTP_RESPONSE_CODE, message.getStatus());
		headers.put(Exchange.CONTENT_TYPE, "application/json+fhir");

		return message.getBody();
	}

}
