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
import uk.org.openeyes.oink.messaging.RabbitRoute;

/**
 * A Facade that handles requests for resources in requests that have no service specified.
 * 
 * i.e. <domain>/<appContext>/resource..
 */
public class NoServiceFacade extends Facade {

	public NoServiceFacade(HttpMapper<RabbitRoute> mapper) {
		super(mapper);
	}

	@Override
	public String getFhirBase() {
		return "/";
	}

	@Override
	protected String getFhirPartFromPath(HttpServletRequest servletRequest) {
		String pathWithinHandler = (String) servletRequest
				.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return pathWithinHandler.substring(pathWithinHandler.indexOf("/")+1);
	}

	@Override
	protected String getDestinationService(HttpServletRequest servletRequest) {
		return null;
	}

}
