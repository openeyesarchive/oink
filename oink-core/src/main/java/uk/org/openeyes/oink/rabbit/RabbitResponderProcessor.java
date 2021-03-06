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
package uk.org.openeyes.oink.rabbit;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitResponderProcessor {
	
	private final Logger log = LoggerFactory.getLogger(RabbitResponderProcessor.class);
	
	public void prepareHeaders(Exchange ex) {
		String replyTo = (String) ex.getIn().getHeader("rabbitmq.REPLY_TO");
		ex.getIn().removeHeader("rabbitmq.REPLY_TO");
		if (replyTo == null || replyTo.isEmpty()) {
			log.warn("No replyTo routingKey found. No response will be sent");
		} else {
			log.debug("Replying to routingKey:"+replyTo);
		}
		ex.getIn().setHeader("rabbitmq.ROUTING_KEY", replyTo);
		ex.getIn().setHeader("rabbitMQ.CONTENT_TYPE", "application/json");
	}

}
