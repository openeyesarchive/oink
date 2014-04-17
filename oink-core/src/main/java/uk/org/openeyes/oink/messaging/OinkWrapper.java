package uk.org.openeyes.oink.messaging;

import javax.security.auth.Subject;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
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

	public void httpToOinkResponse(Exchange e) {

	}

	public void oinkToHttpRequest(Exchange e) {
		
	}

	/**
	 * Takes an OinkResponseMessage and returns a Fhir Response
	 * @param e
	 */
	public void oinkToHttpResponse(Exchange e) {
		OINKResponseMessage message = e.getIn().getBody(OINKResponseMessage.class);
		
		e.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, message.getStatus());
		e.getOut().setHeader(Exchange.CONTENT_TYPE, "application/json+fhir");
		e.getOut().setBody(message.getBody());
	}

}
