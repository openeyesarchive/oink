package uk.org.openeyes.oink.filterchain;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ContextBase;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

/**
 * A custom context used by {@link FilterChain} to pass variables between {@link Command}s.
 * @author Oli
 */
public class FilterChainContext extends ContextBase {
	
	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	private static final long serialVersionUID = 1L;
	
	public static String EXCEPTION_KEY = "exception";
    public static String OINKREQUEST_KEY = "request";
    public static String OINKRESPONSE_KEY = "reponse";
    public static String HTTPREQUEST_KEY = "httpRequest";
    public static String HTTPRESPONSE_KEY = "httpResponse";
 
    private Exception exception;
    private OINKRequestMessage request;
    private OINKResponseMessage response;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    
	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}
	public HttpResponse getHttpResponse() {
		return httpResponse;
	}
	public void setHttpResponse(HttpResponse httpResponse) {
		this.httpResponse = httpResponse;
	}
    
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
