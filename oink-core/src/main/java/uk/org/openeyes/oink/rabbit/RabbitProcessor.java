package uk.org.openeyes.oink.rabbit;

import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.OutHeaders;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.facade.RoutingService;

/**
 * Prepares an OINKRequestMessage for sending over a rabbit broker.
 * @author Oliver Wilkie
 */
public class RabbitProcessor {
	
	private RoutingService rabbitRoutingService;
	
	public RabbitProcessor(RoutingService service) {
		this.rabbitRoutingService = service;
	}
	
	public OINKRequestMessage setRabbitRouteHeaders(@Body OINKRequestMessage message, @OutHeaders Map<String, Object> headers) throws NoRabbitMappingFoundException {
				
		// Find the routing key and exchange using the requested URI and method.
		RabbitRoute mappedRoute = rabbitRoutingService.getRouting(message.getResourcePath(), message.getMethod());
		
		if (mappedRoute == null) {
			throw new NoRabbitMappingFoundException();
		}
		headers.clear();
		headers.put("rabbitmq.ROUTING_KEY", mappedRoute.getRoutingKey());
		headers.put("rabbitmq.EXCHANGE_NAME", mappedRoute.getExchange());
		
		String replyRoutingKey = rabbitRoutingService.getReplyRoutingKey(message.getResourcePath(), message.getMethod());
		if (replyRoutingKey != null && !replyRoutingKey.isEmpty()) {
			headers.put("rabbitmq.REPLY_TO", replyRoutingKey);
		}
		
		return message;
	}

}
