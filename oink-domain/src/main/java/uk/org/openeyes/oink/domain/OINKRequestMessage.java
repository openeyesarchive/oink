package uk.org.openeyes.oink.domain;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class OINKRequestMessage extends OINKMessage {
	
	String resourcePath;
	HttpMethod method;
	HttpHeaders headers;
	byte[] body;
	
	public OINKRequestMessage(String resourcePath, HttpMethod method,
			HttpHeaders headers, byte[] body) {
		this.resourcePath = resourcePath;
		this.method = method;
		this.headers = headers;
		this.body = body;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	
	
}
