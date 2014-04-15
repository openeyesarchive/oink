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
package uk.org.openeyes.oink.filterchain.command.impl;

import java.io.IOException;

import org.apache.commons.chain.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.filterchain.FilterChainContext;
import uk.org.openeyes.oink.filterchain.command.FilterCommand;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

@Component
public class HttpExecuteCommand extends FilterCommand {

	private static final Logger logger = LoggerFactory
			.getLogger(HttpExecuteCommand.class);

	@Override
	protected boolean execute(FilterChainContext context) throws Exception {
		try {
			HttpRequest request = context.getHttpRequest();
			logger.debug("Executing request:" + request.getUrl().toString() + " method:" + request.getRequestMethod());
			HttpResponse response = request.execute();
			context.setHttpResponse(response);
			return Command.CONTINUE_PROCESSING;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		}
	}

}
