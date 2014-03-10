package uk.org.openeyes.oink.messaging;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;

public class InboundOinkService {
	
	SimpleMessageListenerContainer container;
	MessageListenerAdapter listenerAdapter;
		
	public InboundOinkService(CachingConnectionFactory rabbitConnection, String rabbitQueueName, InboundOinkHandler handler) {
		container = new SimpleMessageListenerContainer(rabbitConnection);
		container.setQueueNames(rabbitQueueName);
		container.setConcurrentConsumers(5);
		listenerAdapter = new MessageListenerAdapter();
		listenerAdapter.setMessageConverter(new Jackson2JsonMessageConverter());
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
