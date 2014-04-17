package uk.org.openeyes.oink.http;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.OINKRequestMessage;

public class FhirJsonValidator {

	private final static String jsonContentType = "application/json+fhir";
	
	public void validateRequest(OINKRequestMessage message) throws Exception {
		validateBody(message);
	}

	public void validateBody(OINKRequestMessage message) throws Exception {
		
		String verb = message.getMethod().toString();
		FhirBody body = message.getBody();
		
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
