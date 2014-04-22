package uk.org.openeyes.oink.messaging;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

/**
 * Simple {@link OINKRequestMessage} handler that responds with a 200-OK and the
 * body of the {@link OINKRequestMessage}.
 * 
 * @author Oliver Wilkie
 */
public class EchoResponder {

	public OINKResponseMessage echo(OINKRequestMessage in) {
		OINKResponseMessage out = new OINKResponseMessage(200);
		out.setBody(in.getBody());
		return out;
	}

}
