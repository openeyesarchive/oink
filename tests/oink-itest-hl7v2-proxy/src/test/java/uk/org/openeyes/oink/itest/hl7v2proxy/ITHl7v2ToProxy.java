package uk.org.openeyes.oink.itest.hl7v2proxy;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

@RunWith(PaxExam.class)
public class ITHl7v2ToProxy {
	
	public void testA01CreatePatientLeadsToANewPatientInEndServer() {
		
		// Load example A01
		
		// Launch HL7v2 Client
		
		// Send A01
		
		// Search for Patient in FHIR Server
		
		// Check details of Patient
		
		// Delete Patient for future tests
		
		fail("Not fully implemented");
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
				features(oinkFeaturesRepo, "oink-adapter-hl7v2"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.hl7v2.cfg", new File("src/test/resources/hl7v2.properties")),
				features(oinkFeaturesRepo, "oink-adapter-proxy"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.proxy.cfg", new File("src/test/resources/proxy.properties")),				
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
