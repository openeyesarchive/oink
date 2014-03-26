/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.messaging;

import org.apache.commons.chain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
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
public class FilterChainInboundOinkHandler extends InboundOinkHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(FilterChainInboundOinkHandler.class);

	private HttpMapper<String> resourceToChainMatcher;

	@Autowired
	private FilterCatalogue catalogue;

	public FilterChainInboundOinkHandler(HttpMapper<String> resourceToChainMatcher) {
		this.resourceToChainMatcher = resourceToChainMatcher;
	}

	/* (non-Javadoc)
	 * @see uk.org.openeyes.oink.messaging.InboundOinkHandler#handleMessage(uk.org.openeyes.oink.domain.OINKRequestMessage)
	 */
	@Override
	public OINKResponseMessage handleMessage(OINKRequestMessage request) {
		logger.debug("Recieved an inbound OINK request");
		// Extract chain key based on Oink Message
		String chainName = getChainKeyFromOinkMessage(request);
		// Get chain from catalogue
		Command chain = catalogue.getCommand(chainName);
		if (chain != null) {
			// Prepare context
			FilterChainContext context = new FilterChainContext();
			context.setRequest(request);
			// Execute
			try {
				chain.execute(context);
			} catch (Exception e) {
				logger.error("An exception occured whilst executing the filter chain");
				return null;
			}
			// Extract response message
			OINKResponseMessage message = context.getResponse();
			// Return message
			return message;
		} else {
			logger.error("No filter chain was found to handle the inbound OINK request");
			return null;
		}
	}

	private String getChainKeyFromOinkMessage(OINKRequestMessage message) {
		String resourcePath = message.getResourcePath();
		HttpMethod method = HttpMethod.valueOf(message.getMethod());
		return resourceToChainMatcher.get(resourcePath, method);
	}

}
