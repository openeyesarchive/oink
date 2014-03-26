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
package uk.org.openeyes.oink.filterchain.command;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import uk.org.openeyes.oink.filterchain.FilterChainContext;

public abstract class FilterCommand implements Command {

	@Override
	public boolean execute(Context context) throws Exception {
		return execute((FilterChainContext) context);
	}
	
	protected abstract boolean execute(FilterChainContext context) throws Exception;

}
