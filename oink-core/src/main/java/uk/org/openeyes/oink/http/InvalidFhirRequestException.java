package uk.org.openeyes.oink.http;

import uk.org.openeyes.oink.exception.HttpStatusCode;
import uk.org.openeyes.oink.exception.OinkException;

@HttpStatusCode(400)
public class InvalidFhirRequestException extends OinkException{
	
	public InvalidFhirRequestException(String m) {
		super(m);
	}
	

}
