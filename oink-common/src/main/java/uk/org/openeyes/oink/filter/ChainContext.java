package uk.org.openeyes.oink.filter;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ContextBase;

import uk.org.openeyes.oink.annotation.FilterChain;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

/**
 * A custom context used by {@link FilterChain} to pass variables between {@link Command}s.
 * @author Oli
 */
public class ChainContext extends ContextBase {
	
	private static final long serialVersionUID = 1L;
	
	public static String EXCEPTION_KEY = "exception";
    public static String OINKREQUEST_KEY = "request";
    public static String OINKRESPONSE_KEY = "reponse";
 
    private Exception exception;
    private OINKRequestMessage request;
    private OINKResponseMessage response;
    
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public OINKRequestMessage getRequest() {
		return request;
	}
	public void setRequest(OINKRequestMessage request) {
		this.request = request;
	}
	public OINKResponseMessage getResponse() {
		return response;
	}
	public void setResponse(OINKResponseMessage response) {
		this.response = response;
	}

}
