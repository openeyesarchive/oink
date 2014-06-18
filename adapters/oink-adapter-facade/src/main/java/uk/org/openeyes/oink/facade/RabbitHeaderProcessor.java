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
package uk.org.openeyes.oink.facade;

import java.util.Map;

import org.apache.camel.Body;
import org.apache.camel.OutHeaders;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.rabbit.NoRabbitMappingFoundException;
import uk.org.openeyes.oink.rabbit.RabbitRoute;

/**
 * Prepares an OINKRequestMessage for sending over a rabbit broker.
 * @author Oliver Wilkie
 */
public class RabbitHeaderProcessor {
	
	private RoutingService rabbitRoutingService;
	
	public RabbitHeaderProcessor(RoutingService service) {
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
		headers.put("rabbitmq.CONTENT_TYPE", "application/json");
		headers.put("rabbitmq.CONTENT_ENCODING", "UTF-8");
		
		
		String replyRoutingKey = rabbitRoutingService.getReplyRoutingKey(message.getResourcePath(), message.getMethod());
		if (replyRoutingKey != null && !replyRoutingKey.isEmpty()) {
			headers.put("rabbitmq.REPLY_TO", replyRoutingKey);
		}
		
		return message;
	}

}
