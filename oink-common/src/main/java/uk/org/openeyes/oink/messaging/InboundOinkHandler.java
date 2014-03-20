package uk.org.openeyes.oink.messaging;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

/**
 * Used to receive, process and send a response.
 * 
 * TODO Can we make the ResponseMessage optional?
 * @author Oliver Wilkie
 *
 */
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