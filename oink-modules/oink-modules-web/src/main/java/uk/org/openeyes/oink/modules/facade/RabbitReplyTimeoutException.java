package uk.org.openeyes.oink.modules.facade;

public class RabbitReplyTimeoutException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public RabbitReplyTimeoutException(String message) {
		super(message);
	}

}
