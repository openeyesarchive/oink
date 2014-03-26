/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
