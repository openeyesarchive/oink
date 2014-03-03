package uk.org.openeyes.oink.messaging;

import org.apache.commons.chain.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.google.api.client.http.HttpStatusCodes;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.filterchain.FilterCatalogue;
import uk.org.openeyes.oink.filterchain.FilterChain;
import uk.org.openeyes.oink.filterchain.FilterChainContext;

/**
 * POJO Handler for receiving {@link OINKResponseMessage}s and routing them to a
 * suitable {@link FilterChain} for processing. The handler then returns any
 * {@link OINKResponseMessage} returned from the {@link FilterChain} to whoever
 * called the Handler.
 * 
 * @author Oliver Wilkie
 * 
 */
public class RabbitListener {

	private HttpMapper<String> resourceToChainMatcher;

	@Autowired
	private FilterCatalogue catalogue;

	public RabbitListener(HttpMapper<String> resourceToChainMatcher) {
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
			FilterChainContext context = new FilterChainContext();
			context.setRequest(request);
			// Execute
			chain.execute(context);
			// Extract response message
			OINKResponseMessage message = context.getResponse();
			// Return message
			return message;
		} catch (Exception e) {
			//OINKResponseMessage.Builder builder = new OINKResponseMessage.Builder();
			//builder.setHTTPStatus(HttpStatusCodes.STATUS_CODE_SERVER_ERROR);
			return null;
		}
	}

	private String getChainKeyFromOinkMessage(OINKRequestMessage message) {
		String resourcePath = message.getResourcePath();
		HttpMethod method = HttpMethod.valueOf(message.getMethod());
		return resourceToChainMatcher.get(resourcePath, method);
	}

}
