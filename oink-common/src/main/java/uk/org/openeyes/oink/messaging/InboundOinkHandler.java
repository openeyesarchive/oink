package uk.org.openeyes.oink.messaging;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

/**
 * Used to receive, process and send a response.
 * 
 * TODO Can we make the ResponseMessage optional?
 * @author Oliver Wilkie
 *
 */
public abstract class InboundOinkHandler {
	
	private static final Logger logger = LoggerFactory
			.getLogger(InboundOinkHandler.class);
	

	/**
	 * Entry method for handler
	 * 
	 * @param request
	 *            the incoming request message
	 * @return the returned response message
	 * @throws Exception
	 */
	public abstract OINKResponseMessage handleMessage(OINKRequestMessage request);
	
	public OINKResponseMessage handleMessage(InvalidOinkMessageException e) {
		logger.warn("Message recieved is not an OINK message");
		return new OINKResponseMessage(HttpStatus.SC_UNPROCESSABLE_ENTITY);
	}

}