package uk.org.openeyes.oink.rabbit;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;

/**
 * 
 * A predicate that evaluates to true if the message in the exchange expects a
 * response over Rabbit
 * 
 * @author Oliver Wilkie
 */
public class IsRabbitRPC implements Predicate, Expression {

	@Override
	public boolean matches(Exchange ex) {
		return ex.getIn().getHeader("rabbitmq.REPLY_TO") != null;
	}

	@Override
	public <T> T evaluate(Exchange ex, Class<T> type) {
		Boolean b = new Boolean(matches(ex));
		return (T) b;
	}




}
