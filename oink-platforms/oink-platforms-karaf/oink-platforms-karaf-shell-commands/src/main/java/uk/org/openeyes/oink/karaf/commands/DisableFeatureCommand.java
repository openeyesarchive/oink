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

import java.io.File;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

@Command(scope="oink", name="disable", description="Disables a module (equivalent to feature:uninstall)")
public class DisableFeatureCommand extends OsgiCommandSupport {

	@Argument(index = 0, name="feature", required=true, description="The feature to be disabled", multiValued=false)
	String featureName = null;
	
	private FeaturesService featuresService;
	
	public void setFeaturesService(FeaturesService featuresService) {
		this.featuresService = featuresService;
	}
	
	@Override
	protected Object doExecute() throws Exception {
		
		if (featuresService == null) {
			throw new Exception("Features Service unavailable");
		}
		
		// Check arguments
		Feature f = featuresService.getFeature(featureName);
		if (f == null) {
			throw new Exception("Feature "+featureName+" not found");
		}
		
		// Check if feature is already running
		if (!featuresService.isInstalled(f)) {
			throw new Exception("Feature "+featureName+" is already disabled");
		}
		
		// Get config pid
		String pid = f.getConfigurations().keySet().iterator().next();
		System.out.println("Feature "+featureName+" uses settings:"+pid);
		
		File base = new File(System.getProperty("karaf.base"));
		File cfgFolder = new File(base, "etc");
		File settings = new File(cfgFolder, pid+".cfg");

		featuresService.uninstallFeature(featureName);
		
		// Print result message
		if (!featuresService.isInstalled(f)) {
			System.out.println("Disabled "+featureName+". Configuration file remains at "+settings.getAbsolutePath());
		} else {
			throw new Exception("Could not disable "+featureName);
		}
		
		return null;
	}

	
}
