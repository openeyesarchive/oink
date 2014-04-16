package uk.org.openeyes.oink.http;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;

import uk.org.openeyes.oink.domain.FhirBody;

public class FhirJsonValidator {

	private final static String jsonContentType = "application/json+fhir";

	public void validateRequestHeaders(
			@Header(Exchange.HTTP_PATH) String uriPath,
			@Header(Exchange.HTTP_METHOD) String verb,
			@Header(Exchange.CONTENT_TYPE) String contentType) throws Exception {

		// Check content-type
		if (verb.equals("PUT") || verb.equals("POST")) {
			if (!contentType.equals(jsonContentType)) {
				throw new InvalidRestOperation(
						"Invalid Content Type. Only application/json+fhir is supported");
			}
		}

	}

	public void validateBody(@Body FhirBody body, @Header(Exchange.HTTP_METHOD) String verb) throws Exception {
		// Check content-type
		if (verb.equals("PUT") || verb.equals("POST")) {
			if (body == null) {
				throw new InvalidRestOperation("Invalid Body. A body is required for the verb: "+verb);
			}
		} else {
			if (body != null) {
				throw new InvalidRestOperation("Body detected. A body is not applicable for the verb: "+verb);
			}
		}
	}

}
