package uk.org.openeyes.oink.facade;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import uk.org.openeyes.oink.exception.HttpStatusCode;

/**
 * A custom exception handler that sets the HTTP response code to be returned to
 * the caller using annotation inspection of the exception.
 * 
 * @author Oliver Wilkie
 */
public class WebExceptionProcessor implements Processor {

	private static final int DEFAULT_ERROR_CODE = 500;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		
		int errorCode = DEFAULT_ERROR_CODE;
		
		if (e.getClass().isAnnotationPresent(HttpStatusCode.class)) {
			HttpStatusCode statusCode = e.getClass().getAnnotation(HttpStatusCode.class);
			errorCode = statusCode.value();
		}
		
		exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, errorCode);
		exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "text/plain");
		exchange.getOut().setBody(e.getMessage());
	}
	
	

}
