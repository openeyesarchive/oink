package uk.org.openeyes.oink.exception;

@HttpStatusCode(500)
public class OinkException extends Exception {

	public OinkException() {
		super();
	}
	
	public OinkException(String m) {
		super(m);
	}
}
