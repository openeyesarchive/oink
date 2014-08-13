package uk.org.openeyes.oink.hl7v2;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.hl7.AckCode;
import org.apache.camel.component.hl7.AckExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.PipeParser;

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
	private final PipeParser pipeParser = new PipeParser();
	
	@Override
	public void process(Exchange exchange) throws Exception {

		Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
				Exception.class);
        
        if(log.isDebugEnabled()) {
            log.debug(e.getMessage(), e);
        }
		
		// Send to dead letter queue
		log.info("Sending message to Dead Letter Queue");
		Message message = exchange.getIn().getBody(Message.class);
		String s = pipeParser.encode(message); // TODO What encoding should this be?
		ProducerTemplate prodTemplate = exchange.getContext().createProducerTemplate();
		prodTemplate.sendBody("direct:hl7-consumer-dead-letter", s); // TODO Should there be some rabbit headers to?
		
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
