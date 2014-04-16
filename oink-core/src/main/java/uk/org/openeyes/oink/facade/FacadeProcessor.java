package uk.org.openeyes.oink.facade;

import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.OutHeaders;

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
	
	public OINKRequestMessage setRabbitRouteHeaders(@Body OINKRequestMessage message, @OutHeaders Map<String,Object> outHeaders) throws NoRabbitMappingFoundException {
		
		// Find the routing key and exchange using the requested URI and method.
		RabbitRoute mappedRoute = rabbitRoutingService.getRouting(message.getResourcePath(), message.getMethod());
		
		if (mappedRoute == null) {
			throw new NoRabbitMappingFoundException();
		}
		
		outHeaders.put("rabbitmq.ROUTING_KEY", mappedRoute.getRoutingKey());
		outHeaders.put("rabbitmq.EXCHANGE_NAME", mappedRoute.getExchange());
		
		return message;
	}

}
