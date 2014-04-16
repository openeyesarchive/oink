package uk.org.openeyes.oink.common;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public class HttpMapperEntry<T> {
	
	private String uri;
	private HttpMethod method;
	private T value;
	
	public HttpMapperEntry(String uri, HttpMethod method, T value) {
		super();
		this.uri = uri;
		this.method = method;
		this.value = value;
	}

	public final String getUri() {
		return uri;
	}

	public final HttpMethod getMethod() {
		return method;
	}

	public final T getValue() {
		return value;
	}
	
	
	

}
