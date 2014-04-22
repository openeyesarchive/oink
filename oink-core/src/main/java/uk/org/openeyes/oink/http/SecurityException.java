package uk.org.openeyes.oink.http;

import uk.org.openeyes.oink.exception.HttpStatusCode;
import uk.org.openeyes.oink.exception.OinkException;

@HttpStatusCode(403)
public class SecurityException extends OinkException {

	public SecurityException(String m) {
		super(m);
	}
}
