package uk.org.openeyes.oink.http;

import uk.org.openeyes.oink.exception.HttpStatusCode;
import uk.org.openeyes.oink.exception.OinkException;

@HttpStatusCode(500)
public class InvalidFhirResponseException extends OinkException {

	public InvalidFhirResponseException(String message) {
		super(message);
	}

}
