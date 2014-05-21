package uk.org.openeyes.oink.http;

import org.apache.camel.Exchange;
import org.apache.camel.spi.HeaderFilterStrategy;

public class HttpFilterStrategy implements HeaderFilterStrategy {

	@Override
	public boolean applyFilterToCamelHeaders(String headerName, Object headerValue,
			Exchange exchange) {
		return headerName.startsWith("rabbit");
	}

	@Override
	public boolean applyFilterToExternalHeaders(String arg0, Object arg1,
			Exchange arg2) {
		return false;
	}

}
