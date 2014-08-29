package uk.org.openeyes.oink.hl7v2;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.hl7.AckCode;
import org.apache.camel.component.hl7.AckExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.model.Message;

/**
 * 
 * Takes exceptions thrown by the adapter, stores them in a dead letter queue
 * and returns an AE or AR to the original upstream server.
 * 
 * @author Oliver Wilkie
 */
public class Hl7ExceptionProcessor implements Processor {
	
	private Logger log = LoggerFactory.getLogger(Hl7ExceptionProcessor.class);
	
	private final AckExpression ackError = new AckExpression(AckCode.AE);
	private final AckExpression ackRejected = new AckExpression(AckCode.AR);
	
	@Override
	public void process(Exchange exchange) throws Exception {

		Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
				Exception.class);
        
        if(log.isDebugEnabled()) {
            log.debug(e.getMessage(), e);
        	log.debug("Stack trace: {}", e.getStackTrace().toString());
        }
		
		// Send to dead letter queue
		log.info("Sending message to Dead Letter Queue");
		
		// TODO: add back sending of headers
		/*Map<String, Object> headers = exchange.getIn().getHeaders();

		if(headers.containsKey("rabbitmq.ROUTING_KEY")) {
			headers.remove("rabbitmq.ROUTING_KEY");
		}
		if(headers.containsKey("rabbitmq.EXCHANGE_NAME")) {
			headers.remove("rabbitmq.EXCHANGE_NAME");
		}*/
		
		ProducerTemplate prodTemplate = exchange.getContext().createProducerTemplate();
		//prodTemplate.sendBodyAndHeaders("direct:hl7-consumer-dead-letter", exchange.getIn().getBody(), headers);
		prodTemplate.sendBody("direct:hl7-consumer-dead-letter", exchange.getIn().getBody());
		
		// Prepare response for Hl7
		log.info("Sending response to upstream Hl7 Server");
		Message response = null;
		if (e instanceof UnsupportedHl7v2MessageException) {
			response = ackRejected.evaluate(exchange, Message.class);
		} else {
			response = ackError.evaluate(exchange, Message.class);
		}
		exchange.getIn().setBody(response);
	}

}
