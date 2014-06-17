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
import org.apache.camel.spi.HeaderFilterStrategy;

/**
 * Basic Camel Header Filter Strategy that, when attached to an endpoint, omits
 * any RabbitMQ-related headers.
 * 
 * @author Oliver Wilkie
 */
public class NoRabbitHeadersFilterStrategy implements HeaderFilterStrategy {

	@Override
	public boolean applyFilterToCamelHeaders(String headerName,
			Object headerValue, Exchange exchange) {
		return headerName.startsWith("rabbit");
	}

	@Override
	public boolean applyFilterToExternalHeaders(String arg0, Object arg1,
			Exchange arg2) {
		return false;
	}

}
