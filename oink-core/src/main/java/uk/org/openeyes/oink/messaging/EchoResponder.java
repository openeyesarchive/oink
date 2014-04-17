package uk.org.openeyes.oink.messaging;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

public class EchoResponder {

	public OINKResponseMessage echo(OINKRequestMessage in) {
		OINKResponseMessage out = new OINKResponseMessage(200);
		out.setBody(in.getBody());
		return out;
	}

}
