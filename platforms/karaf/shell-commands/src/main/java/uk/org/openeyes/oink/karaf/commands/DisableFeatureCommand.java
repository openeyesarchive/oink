package uk.org.openeyes.oink.karaf.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import javax.inject.Inject;

import org.apache.karaf.features.ConfigFileInfo;
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
