package uk.org.openeyes.oink.rabbit;

import org.apache.camel.Exchange;
import org.apache.camel.spi.HeaderFilterStrategy;

/**
 * Basic Camel Header Filter Strategy that, when attached to an endpoint, omits
 * any RabbitMQ-related headers.
 * 
 * @author Oliver Wilkie
 */
public class NoRabbitHeadersFilterStrategy implements HeaderFilterStrategy {

	@Override
	public boolean applyFilterToCamelHeaders(String headerName,
			Object headerValue, Exchange exchange) {
		return headerName.startsWith("rabbit");
	}

	@Override
	public boolean applyFilterToExternalHeaders(String arg0, Object arg1,
			Exchange arg2) {
		return false;
	}

}
