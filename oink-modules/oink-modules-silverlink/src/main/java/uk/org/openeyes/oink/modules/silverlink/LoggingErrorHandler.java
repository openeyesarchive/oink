package uk.org.openeyes.oink.modules.silverlink;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.ErrorHandler;

public class LoggingErrorHandler implements ErrorHandler {
		
	private static Logger log = Logger.getLogger(LoggingErrorHandler.class.getName());

	@Override
	public void handleError(Throwable arg0) {
		log.log(Level.SEVERE, "Could not handle incoming message");
	}

}
