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

import java.util.UUID;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

public class SynchronousRabbitProcessor {

	private int timeOut;
	
	public SynchronousRabbitProcessor(int timeOut) {
		this.timeOut = timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public void prepare(Exchange e) {
		// generate correlation id of synchronous request
		final String synchronousRequestId = UUID.randomUUID().toString();

		e.getIn().setHeader("rabbitmq.CORRELATIONID", synchronousRequestId);
	}

	public void waitForResponse(Exchange e) throws SynchronousRabbitTimeoutException {
		CamelContext camelContext = e.getContext();

		final String synchronousRequestId = e.getIn().getHeader(
				"rabbitmq.CORRELATIONID", String.class);

		// wait for service result; null will returned if defined request time
		// timed out
		Exchange resultFromQueuingSystem = camelContext
				.createConsumerTemplate().receive(
						"seda:" + synchronousRequestId, timeOut);

		if (resultFromQueuingSystem != null) {
			
			e.setOut(resultFromQueuingSystem.getIn());

		} else {
			throw new SynchronousRabbitTimeoutException();
		}
	}

}
