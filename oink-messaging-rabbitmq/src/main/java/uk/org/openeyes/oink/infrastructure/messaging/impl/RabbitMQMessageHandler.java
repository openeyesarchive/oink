/*******************************************************************************
 * OpenEyes Interop Toolkit
 * Copyright (C) 2013  OpenEyes Foundation (http://www.openeyes.org.uk)
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
 ******************************************************************************/
package uk.org.openeyes.oink.infrastructure.messaging.impl;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JsonMessageConverter;

import uk.org.openeyes.oink.infrastructure.commands.Gate;

public class RabbitMQMessageHandler implements MessageListener {

	@Inject
	Gate gate;
	
	private final JsonMessageConverter converterJSON = new JsonMessageConverter();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMQMessageHandler.class);
	
	@Inject
	ConnectionFactory connectionFactory; 
	
	@Override
	public void onMessage(Message message) {
		
		Object command = null;
		if(message.getMessageProperties().getContentType().equalsIgnoreCase("application/json")) {
			// Convert from JSON
			command = converterJSON.fromMessage(message);
		} else if(message.getMessageProperties().getContentType().equalsIgnoreCase("application/xml")) {
			// Convert from XML
			// TODO - use MarshallingMessageConverter??
		}
		
		// Get the message
		Object response = gate.dispatch(command);

		// Send the response
		if(response != null && message.getMessageProperties() != null && message.getMessageProperties().getReplyTo() != null) {
			RabbitTemplate template = new RabbitTemplate(connectionFactory);
			
			MessageProperties messageProperties = new MessageProperties();
			
			Message messageReply = converterJSON.toMessage(response, messageProperties);
			
			LOGGER.debug("Reply-to: exchange = '{}' routing key = '{}'", message.getMessageProperties().getReplyToAddress().getExchangeName(), message.getMessageProperties().getReplyToAddress().getRoutingKey());
	
			template.send(message.getMessageProperties().getReplyToAddress().getExchangeName(),
					message.getMessageProperties().getReplyToAddress().getRoutingKey(),
					messageReply);
		}
	}
}
