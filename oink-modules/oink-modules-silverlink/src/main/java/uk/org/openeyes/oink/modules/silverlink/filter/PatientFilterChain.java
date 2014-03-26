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
package uk.org.openeyes.oink.modules.silverlink.filter;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;

import uk.org.openeyes.oink.filterchain.FilterChain;
import uk.org.openeyes.oink.filterchain.command.impl.HttpExecuteCommand;
import uk.org.openeyes.oink.filterchain.command.impl.HttpResponseToOinkResponseCommand;
import uk.org.openeyes.oink.filterchain.command.impl.OinkRequestToHttpRequestCommand;

@FilterChain(name = PatientFilterChain.FILTER_KEY)
public class PatientFilterChain extends ChainBase {
	public final static String FILTER_KEY = "silverlinkPatientFilterChain";

	@Autowired
	public PatientFilterChain(OinkRequestToHttpRequestCommand a, BuildOpenMapsUrlCommand bb,
			HttpExecuteCommand b, HttpResponseToOinkResponseCommand c) {
		super(new Command[] { a, bb, b, c });
	}
}
