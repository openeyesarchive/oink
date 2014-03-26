/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
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
