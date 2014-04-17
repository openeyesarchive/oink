package uk.org.openeyes.oink.facade;

import java.util.Map;

import org.apache.camel.Exchange;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public class FacadeProcessor {
	
	private RoutingService rabbitRoutingService;
	
	public FacadeProcessor(RoutingService service) {
		this.rabbitRoutingService = service;
	}
	
	public void stripFacadeUriBase(Exchange ex) {
		String uri = ex.getIn().getHeader(Exchange.HTTP_PATH, String.class);
		if (uri.startsWith("/")) {
			uri = uri.substring(1);
		}
		ex.getIn().setHeader(Exchange.HTTP_PATH, uri);
	}
	
	public void setRabbitRouteHeaders(Exchange exchange) throws NoRabbitMappingFoundException {
		
		Map<String,Object> headers = exchange.getIn().getHeaders();
		OINKRequestMessage message = exchange.getIn().getBody(OINKRequestMessage.class);
		
		// Find the routing key and exchange using the requested URI and method.
		RabbitRoute mappedRoute = rabbitRoutingService.getRouting(message.getResourcePath(), message.getMethod());
		
		if (mappedRoute == null) {
			throw new NoRabbitMappingFoundException();
		}
		headers.clear();
		headers.put("rabbitmq.ROUTING_KEY", mappedRoute.getRoutingKey());
		headers.put("rabbitmq.EXCHANGE_NAME", mappedRoute.getExchange());
		
	}

}
