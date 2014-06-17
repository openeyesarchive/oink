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
package uk.org.openeyes.oink.rabbit;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.exception.OinkExceptionStatusCode;

/**
 * Takes exceptions thrown by the adapter, wraps it in an OinkResponseMessage and routes it back to
 * the original caller. (RPC-only)
 * 
 * @author Oliver Wilkie
 */
public class OinkRPCExceptionProcessor implements Processor {

	private static final int DEFAULT_ERROR_CODE = 500;

	private static final Logger log = LoggerFactory
			.getLogger(OinkRPCExceptionProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT,
				Exception.class);

		if (e == null) {
			log.warn("Processor called but no exception found");
			return;
		}

		String replyToHeader = exchange.getIn().getHeader("rabbitmq.REPLY_TO",
				String.class);
		if (replyToHeader == null || replyToHeader.isEmpty()) {
			log.info("No REPLY_TO in header. Exception message will not be returned");
			return;
		}

		Integer errorCode;
		if (e.getClass().isAnnotationPresent(OinkExceptionStatusCode.class)) {
			OinkExceptionStatusCode statusCode = e.getClass().getAnnotation(
					OinkExceptionStatusCode.class);
			errorCode = statusCode.value();
		} else {
			errorCode = DEFAULT_ERROR_CODE;
		}

		OINKResponseMessage message = new OINKResponseMessage(errorCode);
		exchange.getOut().setBody(message);
		exchange.getOut().setHeader("rabbitmq.ROUTING_KEY", replyToHeader);
		exchange.getOut().setHeader("rabbitmq.CORRELATIONID",
				exchange.getIn().getHeader("rabbitmq.CORRELATIONID"));
		exchange.getOut().setHeader("rabbitmq.CONTENT_TYPE", "application/json");
	}

}
