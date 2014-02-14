package uk.org.openeyes.oink.messaging;

import org.apache.commons.chain.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import uk.org.openeyes.oink.annotation.FilterChain;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.filter.ChainCatalogue;
import uk.org.openeyes.oink.filter.ChainContext;
import uk.org.openeyes.oink.map.HttpMatcher;

/**
 * POJO Handler for receiving {@link OINKResponseMessage}s and routing them to a
 * suitable {@link FilterChain} for processing. The handler then returns any
 * {@link OINKResponseMessage} returned from the {@link FilterChain} to whoever
 * called the Handler.
 * 
 * @author Oliver Wilkie
 * 
 */
public class Handler {

	private final HttpMatcher<String> resourceToChainMatcher;

	@Autowired
	private ChainCatalogue catalogue;

	public Handler(HttpMatcher<String> resourceToChainMatcher) {
		this.resourceToChainMatcher = resourceToChainMatcher;
	}

	/**
	 * Entry method for handler
	 * 
	 * @param request
	 *            the incoming request message
	 * @return the returned response message
	 * @throws Exception
	 */
	public OINKResponseMessage handle(OINKRequestMessage request) {
		try {
			// Extract chain key based on Oink Message
			String chainName = getChainKeyFromOinkMessage(request);
			// Get chain from catalogue
			Command chain = catalogue.getCommand(chainName);
			// Prepare context
			ChainContext context = new ChainContext();
			context.setRequest(request);
			// Execute
			chain.execute(context);
			// Extract response message
			OINKResponseMessage message = context.getResponse();
			// Return message
			return message;
		} catch (Exception e) {
			OINKResponseMessage.Builder builder = new OINKResponseMessage.Builder();
			builder.setHTTPStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			return builder.build();
		}
	}

	private String getChainKeyFromOinkMessage(OINKRequestMessage message) {
		String resourcePath = message.getResourcePath();
		HttpMethod method = message.getMethod();
		return resourceToChainMatcher.get(resourcePath, method);
	}

}
