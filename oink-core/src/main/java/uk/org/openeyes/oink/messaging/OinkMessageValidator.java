package uk.org.openeyes.oink.messaging;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.http.InvalidFhirMessageException;
import uk.org.openeyes.oink.http.InvalidRestOperation;

public class OinkMessageValidator {
	
	public void validateRequest(OINKRequestMessage message) throws Exception {
		validateBody(message);
	}

	public void validateBody(OINKRequestMessage message) throws Exception {
		
		String verb = message.getMethod().toString();
		FhirBody body = message.getBody();
		
		// Check content-type
		if (verb.equals("PUT") || verb.equals("POST")) {
			if (body == null) {
				throw new InvalidFhirMessageException("Invalid Body. A body is required for the verb: "+verb);
			}
		} else {
			if (body != null) {
				throw new InvalidFhirMessageException("Body detected. A body is not applicable for the verb: "+verb);
			}
		}
	}

}
