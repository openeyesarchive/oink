package uk.org.openeyes.oink.domain;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class OINKResponseMessage extends OINKMessage {
	
	HttpStatus status;
	HttpHeaders headers;
	byte[] body;
	
	public OINKResponseMessage(HttpStatus status, HttpHeaders headers,
			byte[] body) {
		this.status = status;
		this.headers = headers;
		this.body = body;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
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
