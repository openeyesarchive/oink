/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
