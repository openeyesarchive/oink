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
package uk.org.openeyes.oink.facade;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.rabbit.RabbitRoute;

/**
 * An implementation of a {@link RoutingService} that handles mapping incoming
 * requests to an outgoing RabbitQueue. It also has an incoming routing key for
 * responses.
 * 
 * A private {@link HttpMapper} does the actual routing work.
 * 
 * @author Oliver Wilkie
 */
public class FacadeRoutingService implements RoutingService {

	private final HttpMapper<RabbitRoute> mappings;

	private final String replyRoutingKey;

	public FacadeRoutingService(HttpMapper<RabbitRoute> mappings, String replyRouting) {
		this.replyRoutingKey = replyRouting;
		this.mappings = mappings;
	}

	@Override
	public RabbitRoute getRouting(String path, HttpMethod method) {
		return mappings.get(path, method);
	}

	@Override
	public String getReplyRoutingKey(String path, HttpMethod method) {
		return replyRoutingKey;
	}

}
