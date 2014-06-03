package uk.org.openeyes.oink.rabbit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.exception.OinkExceptionStatusCode;

/**
 * Takes exceptions thrown by the adapter, wraps it in an OinkResponseMessage and routes it back to
 * the original caller. (RPC-only)
 * 
 * @author Oliver Wilkie
 */
public class OinkRPCExceptionProcessor implements Processor {

	private static final int DEFAULT_ERROR_CODE = 500;

	private static final Logger log = LoggerFactory
			.getLogger(OinkRPCExceptionProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
				Exception.class);

		if (e == null) {
			log.warn("Processor called but no exception found");
			return;
		}

		String replyToHeader = exchange.getIn().getHeader("rabbitmq.REPLY_TO",
				String.class);
		if (replyToHeader == null || replyToHeader.isEmpty()) {
			log.info("No REPLY_TO in header. Exception message will not be returned");
			return;
		}

		Integer errorCode;
		if (e.getClass().isAnnotationPresent(OinkExceptionStatusCode.class)) {
			OinkExceptionStatusCode statusCode = e.getClass().getAnnotation(
					OinkExceptionStatusCode.class);
			errorCode = statusCode.value();
		} else {
			errorCode = DEFAULT_ERROR_CODE;
		}

		OINKResponseMessage message = new OINKResponseMessage(errorCode);
		exchange.getOut().setBody(message);
		exchange.getOut().setHeader("rabbitmq.ROUTING_KEY", replyToHeader);
		exchange.getOut().setHeader("rabbitmq.CORRELATIONID",
				exchange.getIn().getHeader("rabbitmq.CORRELATIONID"));
	}

}
