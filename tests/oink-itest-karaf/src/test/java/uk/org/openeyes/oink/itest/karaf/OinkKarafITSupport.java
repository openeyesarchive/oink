package uk.org.openeyes.oink.itest.karaf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static Logger log = LoggerFactory.getLogger(OinkKarafITSupport.class);
	
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
		Thread.sleep(10000);
		assertTrue(featuresService.isInstalled(feature));

		serviceRegistration.unregister();
		featuresService.uninstallFeature("oink-adapter-"+adapterSuffix);
		
		assertTrue("Should have failed but actually seems to have started", listener.getContextFailed());
	}
	
	public void checkAdapterContextDoesntFailWithCfg(String adapterSuffix, String configSysProperty) throws Exception {
		
		// Make sure facade-feature is installed
		Feature feature = featuresService.getFeature("oink-adapter-"+adapterSuffix);
		assertFalse(featuresService.isInstalled(feature));
		
		// Load cfg
		Properties properties = getPropertiesBySystemProperty(configSysProperty);
		// Place cfg
		org.osgi.service.cm.Configuration c = configurationAdmin.getConfiguration("uk.org.openeyes.oink."+adapterSuffix);
		assertNull("Existing configuration found",c.getProperties());
		c.update(properties);
		
		// Prepare listener
		ContextListener listener = new ContextListener();
		ServiceRegistration serviceRegistration = bundleContext.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
		
		// Wait for feature to install
		featuresService.installFeature("oink-adapter-"+adapterSuffix);
		Thread.sleep(10000);
		assertTrue(adapterSuffix + " adapter could not be installed",featuresService.isInstalled(feature));

		// Uninstall application, config and listener
		serviceRegistration.unregister();
		featuresService.uninstallFeature("oink-adapter-"+adapterSuffix);
		c.delete();
		
		assertFalse("Context failed to start",listener.getContextFailed());
	}		
	
	public static File getPropertyFileBySystemProperty(String systemProperty) {
		String path = System.getProperty(systemProperty);
		File f = new File(path);
		return f;
	}
	
	public static Properties getPropertiesBySystemProperty(String systemProperty) throws IOException {
		File f = getPropertyFileBySystemProperty(systemProperty);
		if (!f.exists()) {
			throw new FileNotFoundException("No file found at "+ f.getAbsolutePath() + " for system property "+systemProperty+" is it set correctly?");
		}
		FileInputStream fis = new FileInputStream(f);
		Properties p = new Properties();
		p.load(fis);
		fis.close();
		return p;
	}
	
	private class ContextListener implements OsgiBundleApplicationContextListener {

		boolean contextFailed = false;
		
		@Override
		public void onOsgiApplicationEvent(
				OsgiBundleApplicationContextEvent event) {
			log.info("Recieved Spring Context event");
			if (event instanceof OsgiBundleContextFailedEvent) {
				log.info("Spring Context failed to start");
				contextFailed = true;
			}
		}
		
		public boolean getContextFailed() {
			return contextFailed;
		}
		
	}
	
	public Option[] standardConfig() {
		MavenArtifactUrlReference karafUrl = maven()
				.groupId("uk.org.openeyes.oink.karaf").artifactId("distro")
				.version(asInProject()).type("tar.gz");

		MavenUrlReference oinkFeaturesRepo = maven()
				.groupId("uk.org.openeyes.oink.karaf")
				.artifactId("oink-features").version(asInProject()).type("xml")
				.classifier("features");

		MavenUrlReference itSupportClasses = maven()
				.groupId("uk.org.openeyes.oink")
				.artifactId("oink-itest-shared")
				.version(asInProject()).classifier("tests");
		
		String proxyConfig = System.getProperty("it.proxy.config");
		String hl7v2Config = System.getProperty("it.hl7v2.config");
		String facadeToHl7v2Config = System.getProperty("it.facadeToHl7v2.config");
		String facadeToProxyConfig = System.getProperty("it.facadeToProxy.config");
		

		return new Option[] {
				// Provision and launch a container based on a distribution of
				// Karaf (Apache ServiceMix).
				karafDistributionConfiguration().frameworkUrl(karafUrl)
						.unpackDirectory(new File("target/pax"))
						.useDeployFolder(false),
				// Don't bother with local console output as it just ends up
				// cluttering the logs
				configureConsole().ignoreLocalConsole(),
				// Force the log level to INFO so we have more details during
				// the test. It defaults to WARN.
				logLevel(LogLevel.INFO),
				features(
						"mvn:org.apache.karaf.features/spring/3.0.1/xml/features",
						"spring-dm"),
				editConfigurationFilePut("etc/system.properties", "it.proxy.config", proxyConfig),
				editConfigurationFilePut("etc/system.properties", "it.hl7v2.config", hl7v2Config),
				editConfigurationFilePut("etc/system.properties", "it.facadeToHl7v2.config", facadeToHl7v2Config),
				editConfigurationFilePut("etc/system.properties", "it.facadeToProxy.config", facadeToProxyConfig),
			
		// Provision the example feature exercised by this test
		// features(oinkFeaturesRepo, "oink-example-facade"),
		// replaceConfigurationFile("etc/uk.org.openeyes.oink.facade.cfg", new
		// File("src/test/resources/facade.properties")),
		// Remember that the test executes in another process. If you want to
		// debug it, you need
		// to tell Pax Exam to launch that process with debugging enabled.
		// Launching the test class itself with
		// debugging enabled (for example in Eclipse) will not get you the
		// desired results.
		// debugConfiguration("5005", true),
		};
	}
	

}
