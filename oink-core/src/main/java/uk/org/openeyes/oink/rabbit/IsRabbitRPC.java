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
import org.apache.camel.Expression;
import org.apache.camel.Predicate;

/**
 * 
 * A predicate that evaluates to true if the message in the exchange expects a
 * response over Rabbit
 * 
 * @author Oliver Wilkie
 */
public class IsRabbitRPC implements Predicate, Expression {

	@Override
	public boolean matches(Exchange ex) {
		return ex.getIn().getHeader("rabbitmq.REPLY_TO") != null;
	}

	@Override
	public <T> T evaluate(Exchange ex, Class<T> type) {
		Boolean b = new Boolean(matches(ex));
		return (T) b;
	}




}
