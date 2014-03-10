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
import org.springframework.stereotype.Service;

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
			String replyQueueName) {
		super(rabbitConnection);
		setMessageConverter(new Jackson2JsonMessageConverter());
		Queue replyQueue = new Queue(replyQueueName);
		setReplyQueue(replyQueue);
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
