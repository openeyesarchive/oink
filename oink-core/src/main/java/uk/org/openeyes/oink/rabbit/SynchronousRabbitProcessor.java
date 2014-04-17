package uk.org.openeyes.oink.rabbit;

import java.util.UUID;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

public class SynchronousRabbitProcessor {

	private int timeOut;
	
	public SynchronousRabbitProcessor(int timeOut) {
		this.timeOut = timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public void prepare(Exchange e) {
		// generate correlation id of synchronous request
		final String synchronousRequestId = UUID.randomUUID().toString();

		e.getIn().setHeader("rabbitmq.CORRELATIONID", synchronousRequestId);
	}

	public void waitForResponse(Exchange e) {
		CamelContext camelContext = e.getContext();

		final String synchronousRequestId = e.getIn().getHeader(
				"rabbitmq.CORRELATIONID", String.class);

		// wait for service result; null will returned if defined request time
		// timed out
		Exchange resultFromQueuingSystem = camelContext
				.createConsumerTemplate().receive(
						"seda:" + synchronousRequestId, timeOut);

		if (resultFromQueuingSystem != null) {
			
			e.setOut(resultFromQueuingSystem.getIn());

		} else {
			// tell http client the request timed out
			e.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 504);
		}
	}

}
