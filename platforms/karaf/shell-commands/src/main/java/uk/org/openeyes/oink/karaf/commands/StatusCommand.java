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
package uk.org.openeyes.oink.karaf.commands;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import uk.org.openeyes.oink.infrastructure.AdapterStatusService;

@Command(scope="oink", name="status", description="Reports on the health of an adapter")
public class StatusCommand extends OsgiCommandSupport {

	@Argument(index = 0, name="adapter", required=true, description="The adapter to fetch the status of", multiValued=false)
	String featureName = null;
	
	private AdapterStatusService facadeStatus;
	private AdapterStatusService proxyStatus;
	private AdapterStatusService hl7v2Status;
	
	@Override
	protected Object doExecute() throws Exception {
		boolean isActive = false;
		if ("oink-adapter-facade".equals(featureName)) {
			isActive = isAlive(facadeStatus);
		} else if ("oink-adapter-proxy".equals(featureName)) {
			isActive = isAlive(proxyStatus);			
		} else if ("oink-adapter-hl7v2".equals(featureName)) {
			isActive = isAlive(hl7v2Status);			
		} else {
			throw new IllegalArgumentException("Adapter name not recognised");
		}
		System.out.println(isActive ? "Active" : "Not Active");
		return isActive ? 1 : 0;
	}

	private boolean isAlive(AdapterStatusService service) {
		try {
			return service.isAlive();
		} catch (Exception ex) {
			return false;
		}
	}

	public final void setFeatureName(String featureName) {
		this.featureName = featureName;
	}

	public final void setFacadeStatus(AdapterStatusService facadeStatus) {
		this.facadeStatus = facadeStatus;
	}

	public final void setProxyStatus(AdapterStatusService proxyStatus) {
		this.proxyStatus = proxyStatus;
	}

	public final void setHl7v2Status(AdapterStatusService hl7v2Status) {
		this.hl7v2Status = hl7v2Status;
	}

	
}
