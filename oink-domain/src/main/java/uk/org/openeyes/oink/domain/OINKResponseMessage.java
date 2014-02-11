package uk.org.openeyes.oink.domain;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class OINKResponseMessage extends OINKMessage {
	
	HttpStatus status;
	HttpHeaders headers;
	byte[] body;
	
	public OINKResponseMessage() {
		
	}
	
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OINKResponseMessage other = (OINKResponseMessage) obj;
		if (!Arrays.equals(body, other.body))
			return false;
		if (headers == null) {
			if (other.headers != null)
				return false;
		} else if (!headers.equals(other.headers))
			return false;
		if (status != other.status)
			return false;
		return true;
	}
}
