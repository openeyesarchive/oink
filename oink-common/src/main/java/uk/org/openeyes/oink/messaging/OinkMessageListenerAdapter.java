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
package uk.org.openeyes.oink.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;

public class OinkMessageListenerAdapter extends MessageListenerAdapter{
	
	private static final Logger logger = LoggerFactory
			.getLogger(OinkMessageListenerAdapter.class);
	
	/**
	 * Only processes the result if there is somewhere to send it to
	 * @throws Exception 
	 */
	protected void handleResult(Object result, Message request, com.rabbitmq.client.Channel channel) throws Exception {
		try {
			getReplyToAddress(request);
		} catch (AmqpException e) {
			// No reply-to address exists
			logger.warn("An OINK Handler returned an OinkResponseMessage but neither the incoming message or this adapter specified a reply-to address so no response has been posted.");
			return;
		}
		super.handleResult(result, request, channel);
	}

}
