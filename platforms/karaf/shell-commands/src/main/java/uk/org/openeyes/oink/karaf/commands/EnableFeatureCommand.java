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
import java.io.FileOutputStream;
import java.nio.file.Files;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

@Command(scope="oink", name="enable", description="Activates a module using a provided configuration file")
public class EnableFeatureCommand extends OsgiCommandSupport {

	@Argument(index = 0, name="feature", required=true, description="The feature to be enabled", multiValued=false)
	String featureName = null;
	
	@Argument(index = 1, name="cfgPath", description="The path of the configuration file to use", required=false)
	String configPath = null;
	
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
		if (featuresService.isInstalled(f)) {
			throw new Exception("Feature "+featureName+" is already enabled");
		}
		
		// Get config pid
		String pid = f.getConfigurations().keySet().iterator().next();
		System.out.println("Feature "+featureName+" uses settings:"+pid);
		
		File base = new File(System.getProperty("karaf.base"));
		File cfgFolder = new File(base, "etc");
		File settings = new File(cfgFolder, pid+".cfg");

		if (!settings.exists() && configPath == null) {
			throw new Exception("Specify path of config file to use OR populate "+settings.getAbsolutePath());
		}
		
		if (configPath != null) {
			File srcSettings = new File(configPath);
			if (!srcSettings.exists()) {
				throw new Exception("Config file not found: "+configPath);
			}
			if (settings.exists()) {
				settings.delete();
			}
			FileOutputStream fileOs = new FileOutputStream(settings);
			Files.copy(srcSettings.toPath(), fileOs);
			fileOs.close();			
			System.out.println("Configuration copied to "+settings.getAbsolutePath());			
		}
		
		// Start feature
		featuresService.installFeature(featureName);
		
		// Print result message
		if (featuresService.isInstalled(f)) {
			System.out.println("Installed "+featureName+" using configuration at "+settings.getAbsolutePath());
		} else {
			throw new Exception("Could not install "+featureName);
		}
		
		return null;
	}

	
}
