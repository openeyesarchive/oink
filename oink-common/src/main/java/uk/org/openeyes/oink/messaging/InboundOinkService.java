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
