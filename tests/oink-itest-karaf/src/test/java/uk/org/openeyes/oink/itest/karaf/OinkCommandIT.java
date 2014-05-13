package uk.org.openeyes.oink.itest.karaf;

import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;
import static org.ops4j.pax.exam.MavenUtils.asInProject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import javax.inject.Inject;

import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OinkCommandIT {

	private static final Logger logger = LoggerFactory
			.getLogger(OinkCommandIT.class);

	@Inject
	protected BundleContext bundleContext;

	@Inject
	private ConfigurationAdmin configurationAdmin;
	
	@Inject
	private FeaturesService featuresService;
	
	@Inject
	private CommandProcessor commandProcessor;
	
	@Test
	public void testOinkCommandsAreAvailable() throws Exception {
		Feature f = featuresService.getFeature("oink-commands");
		assertNotNull(f);
		assertTrue(featuresService.isInstalled(f));
		
	}
	
	@Test
	public void testOinkEnableCommandIsAvailableOnShellOnAFreshCopyOfCustomDistro() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		PrintStream psout = new PrintStream(out);		
		PrintStream pser = new PrintStream(err);
		CommandSession cs = commandProcessor.createSession(System.in, psout, pser);
		cs.execute("help oink:enable");
		cs.close();
		psout.close();
		pser.close();
		assertTrue(err.toString().isEmpty());
		assertFalse(out.toString().contains("COMMANDS"));		
	}	
	
	@Test
	public void testOinkDisableCommandIsAvailableOnShellOnAFreshCopyOfCustomDistro() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		PrintStream psout = new PrintStream(out);		
		PrintStream pser = new PrintStream(err);
		CommandSession cs = commandProcessor.createSession(System.in, psout, pser);
		cs.execute("help oink:disable");
		cs.close();
		psout.close();
		pser.close();
		assertTrue(err.toString().isEmpty());
		assertFalse(out.toString().contains("COMMANDS"));		
	}	
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
	    probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*,org.apache.felix.service.*;status=provisional,org.springframework.osgi.context.event.*;status=provisional");
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
				//features(oinkFeaturesRepo, "oink-commands"),
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
