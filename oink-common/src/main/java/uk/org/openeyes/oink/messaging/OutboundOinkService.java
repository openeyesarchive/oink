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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

/**
 * A bean service that can send messages outwards to a Rabbit broker and return
 * replies via a fixed reply queue if required. See {@link RabbitTemplate} for more information.
 * 
 * @author Oliver Wilkie
 */
public class OutboundOinkService extends RabbitTemplate {

	private static final Logger logger = LoggerFactory
			.getLogger(OutboundOinkService.class);

	SimpleMessageListenerContainer replyQueueListenerContainer;

	public OutboundOinkService(CachingConnectionFactory rabbitConnection,
			String replyQueueName, String exchangeName) {
		super(rabbitConnection);
		setMessageConverter(new Jackson2JsonMessageConverter());
		Queue replyQueue = new Queue(replyQueueName);
		setReplyQueue(replyQueue);
		setExchange(exchangeName);
		replyQueueListenerContainer = new SimpleMessageListenerContainer(
				rabbitConnection);
		replyQueueListenerContainer.setQueues(replyQueue);
		replyQueueListenerContainer.setMessageListener(this);
	}

	@PostConstruct
	public void init() {
		replyQueueListenerContainer.start();
	}

	@PreDestroy
	public void stopContainer() {
		replyQueueListenerContainer.stop();
	}

}
