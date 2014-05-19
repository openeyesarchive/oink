package uk.org.openeyes.oink.itest.karaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.springframework.osgi.context.event.OsgiBundleContextFailedEvent;


/**
 * 
 * Common Testing methods for testing the deployment of OINK adapters in an OSGI
 * container
 * 
 * @author Oliver
 */
public class OinkKarafITSupport {
	
	@Inject
	private FeaturesService featuresService;
	
	@Inject
	protected BundleContext bundleContext;

	@Inject
	private ConfigurationAdmin configurationAdmin;	
	
	public void checkAdapterHasASingleConfigPidAssociatedInTheFeaturesRepo(String adapterSuffix) throws Exception {
		Feature feature = featuresService.getFeature("oink-adapter-"+adapterSuffix);
		Map<String, Map<String,String>> configurations = feature.getConfigurations();
		assertNotNull(configurations);
		assertEquals(1, configurations.size());
		assertTrue(configurations.containsKey("uk.org.openeyes.oink."+adapterSuffix));
	}
	
	public void checkAdapterContextFailsWithoutCfg(String adapterSuffix) throws Exception {
		
		// Make sure facade-feature is installed
		Feature feature = featuresService.getFeature("oink-adapter-"+adapterSuffix);
		assertFalse(featuresService.isInstalled(feature));
		
		// Prepare listener
		ContextListener listener = new ContextListener();
		ServiceRegistration serviceRegistration = bundleContext.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
		
		// Wait for feature to install
		featuresService.installFeature("oink-adapter-"+adapterSuffix);
		Thread.sleep(5000);
		assertTrue(featuresService.isInstalled(feature));

		serviceRegistration.unregister();
		featuresService.uninstallFeature("oink-adapter-"+adapterSuffix);
		
		assertTrue(listener.getContextFailed());
	}
	
	public void checkAdapterContextDoesntFailWithCfg(String adapterSuffix) throws Exception {
		
		// Make sure facade-feature is installed
		Feature feature = featuresService.getFeature("oink-adapter-"+adapterSuffix);
		assertFalse(featuresService.isInstalled(feature));
		
		// Load cfg
		Properties properties = new Properties();
		File f = new File("../../../src/test/resources/"+adapterSuffix+".properties");
		assertTrue(f.exists());
		FileInputStream fileIo = new FileInputStream(f);
		properties.load(fileIo);
		fileIo.close();

		// Place cfg
		org.osgi.service.cm.Configuration c = configurationAdmin.getConfiguration("uk.org.openeyes.oink."+adapterSuffix);
		assertNull("Existing configuration found",c.getProperties());
		c.update(properties);
		
		// Prepare listener
		ContextListener listener = new ContextListener();
		ServiceRegistration serviceRegistration = bundleContext.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
		
		// Wait for feature to install
		featuresService.installFeature("oink-adapter-"+adapterSuffix);
		Thread.sleep(5000);
		assertTrue(adapterSuffix + " adapter could not be installed",featuresService.isInstalled(feature));

		// Uninstall application, config and listener
		serviceRegistration.unregister();
		featuresService.uninstallFeature("oink-adapter-"+adapterSuffix);
		c.delete();
		
		assertFalse("Context failed to start",listener.getContextFailed());
	}		
	
	private class ContextListener implements OsgiBundleApplicationContextListener {

		boolean contextFailed = false;
		
		@Override
		public void onOsgiApplicationEvent(
				OsgiBundleApplicationContextEvent event) {
			if (event instanceof OsgiBundleContextFailedEvent) {
				contextFailed = true;
			}
		}
		
		public boolean getContextFailed() {
			return contextFailed;
		}
		
	}
	

}
