package uk.org.openeyes.oink.itest.adapters;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.PaxExamRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.it.ITSupport;

/*
 * Tests the behaviour of the proxy adapter when deployed on the custom Karaf distro
 */
public class ITHl7v2Adapter {

	private static Properties props;
	private static final Logger log = LoggerFactory
			.getLogger(ITHl7v2Adapter.class);

	private static TestContainer examContainer;

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {
		props = ITSupport.getPropertiesBySystemProperty("it.hl7v2.config");

		// Start Pax Exam
		ExamSystem system = PaxExamRuntime.createServerSystem(config());
		examContainer = PaxExamRuntime.createContainer(system);
		examContainer.start();
		
		// TODO Fix - For some reason a large wait is required
		Thread.sleep(45000);
	}

	@AfterClass
	public static void tearDown() {
		examContainer.stop();
	}

	@Before
	public void before() throws InterruptedException {
		// Even though the bundles have started, the spring dm contexts may not
		// be started yet so we must wait
		Thread.sleep(15000);
	}

	@Configuration
	public static Option[] config() {
		MavenArtifactUrlReference karafUrl = maven()
				.groupId("uk.org.openeyes.oink.karaf").artifactId("distro")
				.version(asInProject()).type("tar.gz");

		MavenUrlReference oinkFeaturesRepo = maven()
				.groupId("uk.org.openeyes.oink.karaf")
				.artifactId("oink-features").version(asInProject()).type("xml")
				.classifier("features");

		return new Option[] {
				// Provision and launch a container based on a distribution of
				// Karaf (Apache ServiceMix).
				karafDistributionConfiguration().frameworkUrl(karafUrl)
						.unpackDirectory(new File("target/pax"))
						.useDeployFolder(false),
				// It is really nice if the container sticks around after the
				// test so you can check the contents
				// of the data directory when things go wrong.
				//keepRuntimeFolder(),
				// Don't bother with local console output as it just ends up
				// cluttering the logs
				// configureConsole().ignoreLocalConsole(),
				// Force the log level to INFO so we have more details during
				// the test. It defaults to WARN.
				// Provision the example feature exercised by this test
				features(oinkFeaturesRepo, "oink-adapter-hl7v2"),
				replaceConfigurationFile(
						"etc/uk.org.openeyes.oink.hl7v2.cfg",
						ITSupport.getPropertyFileBySystemProperty("it.hl7v2.config")),
				replaceConfigurationFile("etc/org.ops4j.pax.logging.cfg",
						new File("src/test/resources/log4j.properties")),

		// Remember that the test executes in another process. If you
		// want to
		// debug it, you need
		// to tell Pax Exam to launch that process with debugging
		// enabled.
		// Launching the test class itself with
		// debugging enabled (for example in Eclipse) will not get you
		// the
		// desired results.
		// KarafDistributionOption.debugConfiguration("8889", true),
		};

	}

}
