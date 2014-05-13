package uk.org.openeyes.oink.itest.karaf;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.MavenUtils.asInProject;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.apache.karaf.features.ConfigFileInfo;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.springframework.osgi.context.event.OsgiBundleContextFailedEvent;

@RunWith(PaxExam.class)
public class OinkFacadeIT {

	private static final Logger logger = LoggerFactory
			.getLogger(OinkFacadeIT.class);

	@Inject
	protected BundleContext bundleContext;

	@Inject
	private ConfigurationAdmin configurationAdmin;
	
	@Inject
	private FeaturesService featuresService;
	
	@Test
	public void checkFacadeHasASingleConfigPidAssociatedInTheFeaturesRepo() throws Exception {
		Feature feature = featuresService.getFeature("oink-adapter-facade");
		Map<String, Map<String,String>> configurations = feature.getConfigurations();
		assertNotNull(configurations);
		assertEquals(1, configurations.size());
		assertTrue(configurations.containsKey("uk.org.openeyes.oink.facade"));
	}
	
	@Test
	public void checkFacadeContextFailsWithoutCfg() throws Exception {
		
		// Make sure facade-feature is installed
		Feature feature = featuresService.getFeature("oink-adapter-facade");
		assertFalse(featuresService.isInstalled(feature));
		
		// Prepare listener
		ContextListener listener = new ContextListener();
		ServiceRegistration serviceRegistration = bundleContext.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
		
		// Wait for feature to install
		featuresService.installFeature("oink-adapter-facade");
		Thread.sleep(5000);
		assertTrue(featuresService.isInstalled(feature));

		serviceRegistration.unregister();
		featuresService.uninstallFeature("oink-adapter-facade");
		
		assertTrue(listener.getContextFailed());
	}
	
	@Test
	public void checkFacadeContextDoesntFailWithCfg() throws Exception {
		
		// Make sure facade-feature is installed
		Feature feature = featuresService.getFeature("oink-adapter-facade");
		assertFalse(featuresService.isInstalled(feature));
		
		// Load cfg
		Properties properties = new Properties();
		File f = new File("../../../src/test/resources/facade.properties");
		assertTrue(f.exists());
		FileInputStream fileIo = new FileInputStream(f);
		properties.load(fileIo);
		fileIo.close();

		// Place cfg
		org.osgi.service.cm.Configuration c = configurationAdmin.getConfiguration("uk.org.openeyes.oink.facade");
		assertNull(c.getProperties());
		c.update(properties);
		
		// Prepare listener
		ContextListener listener = new ContextListener();
		ServiceRegistration serviceRegistration = bundleContext.registerService(OsgiBundleApplicationContextListener.class.getName(), listener, null);
		
		// Wait for feature to install
		featuresService.installFeature("oink-adapter-facade");
		Thread.sleep(5000);
		assertTrue(featuresService.isInstalled(feature));

		// Uninstall application, config and listener
		serviceRegistration.unregister();
		featuresService.uninstallFeature("oink-adapter-facade");
		c.delete();
		
		assertFalse(listener.getContextFailed());
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
	
	@Ignore
	@Test
	public void checkFeatureIsNotInstalledByDefault() throws Exception {
		Feature feature = featuresService.getFeature("oink-adapter-facade");
		assertFalse(featuresService.isInstalled(feature));
	}
	
//	@Test
//	public void testNoErrorsInDiag() throws Exception {
//		ByteArrayOutputStream outOs = new ByteArrayOutputStream();
//		PrintStream outPs = new PrintStream(outOs);
//		ByteArrayOutputStream errOs = new ByteArrayOutputStream();
//		PrintStream errPs = new PrintStream(errOs);
//		CommandSession cs = commandProcessor.createSession(System.in, outPs, errPs);
//		cs.execute("feature:list");
//		cs.close();
//		assertTrue(outOs.toString().isEmpty());
//		assertTrue(errOs.toString().isEmpty());		
//	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
	    probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*,org.springframework.osgi.context.event.*;status=provisional");
	    return probe;
	}

	@Configuration
	public Option[] config() {
		MavenArtifactUrlReference karafUrl = maven()
				.groupId("uk.org.openeyes.oink.karaf").artifactId("distro")
				.version(asInProject()).type("tar.gz");

		MavenUrlReference oinkFeaturesRepo = maven()
				.groupId("uk.org.openeyes.oink.karaf")
				.artifactId("oink-features").version(asInProject())
				.type("xml").classifier("features");

		return new Option[] {
				// Provision and launch a container based on a distribution of
				// Karaf (Apache ServiceMix).
				karafDistributionConfiguration().frameworkUrl(karafUrl)
						.unpackDirectory(new File("target/pax"))
						.useDeployFolder(false),
				// It is really nice if the container sticks around after the
				// test so you can check the contents
				// of the data directory when things go wrong.
				keepRuntimeFolder(),
				// Don't bother with local console output as it just ends up
				// cluttering the logs
				configureConsole().ignoreLocalConsole(),
				// Force the log level to INFO so we have more details during
				// the test. It defaults to WARN.
				logLevel(LogLevel.INFO),
				features("mvn:org.apache.karaf.features/spring/3.0.1/xml/features", "spring-dm"),
				// Provision the example feature exercised by this test
				//features(oinkFeaturesRepo, "oink-example-facade"),
				//replaceConfigurationFile("etc/uk.org.openeyes.oink.facade.cfg", new File("src/test/resources/facade.properties")),
		// Remember that the test executes in another process. If you want to
		// debug it, you need
		// to tell Pax Exam to launch that process with debugging enabled.
		// Launching the test class itself with
		// debugging enabled (for example in Eclipse) will not get you the
		// desired results.
		//debugConfiguration("5005", true),
		};
	}
	
}
