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