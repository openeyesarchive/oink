package uk.org.openeyes.oink.exception;

@HttpStatusCode(500)
public abstract class OinkException extends Exception {

	public OinkException() {
		super();
	}
	
	public OinkException(String m) {
		super(m);
	}
}
