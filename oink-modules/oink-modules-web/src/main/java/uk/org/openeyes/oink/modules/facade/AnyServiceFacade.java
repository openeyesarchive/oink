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
package uk.org.openeyes.oink.modules.facade;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public class AnyServiceFacade extends Facade {

	public AnyServiceFacade(HttpMapper<RabbitRoute> mapper) {
		super(mapper);
	}

	@Override
	public String getFhirBase() {
		return "/*/";
	}

	@Override
	protected String getFhirPartFromPath(HttpServletRequest servletRequest) {
		String pathWithinHandler = (String) servletRequest
				.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return pathWithinHandler.substring(pathWithinHandler.indexOf("/")+1);
	}

	@Override
	protected String getDestinationService(HttpServletRequest servletRequest) {
		String pathWithinHandler = (String) servletRequest
				.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return pathWithinHandler.substring(0, pathWithinHandler.indexOf("/"));
	}
	
	@Override
	public RabbitRoute getRoute(String resource, HttpMethod method, HttpServletRequest servletRequest) {
		RabbitRoute route = resourceToRabbitRouteMapper.get(resource, method);
		RabbitRoute updatedRoute = new RabbitRoute(route);
		// Replace any wildcards with the service name
		String service = getDestinationService(servletRequest);
		updatedRoute.setRoutingKey(updatedRoute.getRoutingKey().replace("*", service));
		return updatedRoute;
	}

}
