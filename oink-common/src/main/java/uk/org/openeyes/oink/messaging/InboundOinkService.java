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

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

public class InboundOinkService {
	
	SimpleMessageListenerContainer container;
	MessageListenerAdapter listenerAdapter;
		
	public InboundOinkService(CachingConnectionFactory rabbitConnection, String rabbitQueueName, InboundOinkHandler handler) {
		container = new SimpleMessageListenerContainer(rabbitConnection);
		container.setQueueNames(rabbitQueueName);
		container.setConcurrentConsumers(5);
		container.setDefaultRequeueRejected(true);
		container.setChannelTransacted(true);	// Forces messages to be acknowledged
		container.setAcknowledgeMode(AcknowledgeMode.AUTO); // the container will acknowledge the message automatically, unless the MessageListener throws an exception.
		listenerAdapter = new OinkMessageListenerAdapter();
		listenerAdapter.setMessageConverter(new OinkMessageConverter());
		listenerAdapter.setDelegate(handler);
		container.setMessageListener(listenerAdapter);
	}
	
	@PostConstruct
	public void init() {
		container.start();
	}
	
	@PreDestroy
	public void destroy() {
		container.stop();
	}

}
