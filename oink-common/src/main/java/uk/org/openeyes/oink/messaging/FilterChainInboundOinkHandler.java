/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
