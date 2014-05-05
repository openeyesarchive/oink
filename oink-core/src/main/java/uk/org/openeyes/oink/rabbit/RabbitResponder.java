package uk.org.openeyes.oink.rabbit;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitResponder {
	
	private final Logger log = LoggerFactory.getLogger(RabbitResponder.class);
	
	public void prepareHeaders(Exchange ex) {
		String replyTo = (String) ex.getIn().getHeader("rabbitmq.REPLY_TO");
		ex.getIn().removeHeader("rabbitmq.REPLY_TO");
		if (replyTo == null || replyTo.isEmpty()) {
			log.warn("No replyTo routingKey found. No response will be sent");
		} else {
			log.info("Replying to routingKey:"+replyTo);
		}
		ex.getIn().setHeader("rabbitmq.ROUTING_KEY", replyTo);
	}

}
