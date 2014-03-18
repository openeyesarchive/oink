package uk.org.openeyes.oink.messaging;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

public interface InboundOinkHandler {

	/**
	 * Entry method for handler
	 * 
	 * @param request
	 *            the incoming request message
	 * @return the returned response message
	 * @throws Exception
	 */
	public abstract OINKResponseMessage handleMessage(OINKRequestMessage request);

}