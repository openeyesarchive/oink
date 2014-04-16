package uk.org.openeyes.oink.messaging;

import javax.security.auth.Subject;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.security.IdentityService;

public class OinkWrapper {

	private IdentityService identityService;

	public OinkWrapper(IdentityService identityService) {
		this.identityService = identityService;
	}

	/**
	 * Wrap a RESTful Fhir Request into an OinkRequestMessage
	 */
	public OINKRequestMessage wrapRequest(@Header(Exchange.AUTHENTICATION) Subject subject,
			@Header(Exchange.HTTP_PATH) String path,
			@Header(Exchange.HTTP_METHOD) String verb,
			@Header(Exchange.HTTP_QUERY) String query, @Body FhirBody body) {
		OINKRequestMessage message = new OINKRequestMessage();

		// Set OINK Headers
		message.setOrigin(identityService.getOrganisation(subject));

		// Wrap FHIR Request
		message.setResourcePath(path);

		HttpMethod method = HttpMethod.valueOf(verb);
		message.setMethod(method);

		message.setParameters(query);

		message.setBody(body);
		
		return message;

	}

	public void wrapResponse(Exchange e) {

	}

	public void unwrapRequest(Exchange e) {

	}

	public void unwrapResponse(Exchange e) {

	}

}
