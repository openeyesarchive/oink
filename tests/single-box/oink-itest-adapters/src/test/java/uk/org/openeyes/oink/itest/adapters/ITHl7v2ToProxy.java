package uk.org.openeyes.oink.itest.adapters;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ACK;
import uk.org.openeyes.oink.it.ITSupport;
import uk.org.openeyes.oink.test.Hl7Helper;

/**
 * 
 * Tests the intercompability of the standard HL7v2 Adapter and the standard
 * Proxy adapter running on the same Karaf container.
 * 
 * @author Oliver Wilkie
 */
public class ITHl7v2ToProxy {


	private static Properties hl7Props;
	private static Properties proxyProps;
	
	private static TestContainer examContainer;

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {

		hl7Props = ITSupport.getPropertiesBySystemProperty("it.hl7v2.config");
				
		proxyProps = ITSupport.getPropertiesBySystemProperty("it.proxy.config");
		
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

	@Test
	public void testA01CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A01
		Message exampleA01 = Hl7Helper.loadHl7Message("/example-messages/hl7v2/A01-mod.txt");

		// Post A01
		testMessageCanBePostedAndAcceptedByOink(exampleA01);

		// Search for Patient
		IGenericClient client = ITSupport.buildHapiClientForProxy(proxyProps);

		Bundle searchResults = client
				.search()
				.forResource(ca.uhn.fhir.model.dstu.resource.Patient.class)
				.where(ca.uhn.fhir.model.dstu.resource.Patient.IDENTIFIER
						.exactly().identifier("9999999999"))
				.and(ca.uhn.fhir.model.dstu.resource.Patient.GIVEN.matches()
						.value("Test")).execute();
		
		assertEquals(1, searchResults.getEntries().size());

	}

	@Test
	public void testA05CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A05
		Message exampleA05 = Hl7Helper.loadHl7Message("/example-messages/hl7v2/A05-mod.txt");

		// Post A01
		testMessageCanBePostedAndAcceptedByOink(exampleA05);

		// Search for Patient
		IGenericClient client = ITSupport.buildHapiClientForProxy(proxyProps);

		Bundle searchResults = client
				.search()
				.forResource(ca.uhn.fhir.model.dstu.resource.Patient.class)
				.where(ca.uhn.fhir.model.dstu.resource.Patient.IDENTIFIER
						.exactly().identifier("9999999999"))
				.and(ca.uhn.fhir.model.dstu.resource.Patient.GIVEN.matches()
						.value("Testdon")).execute();

		assertEquals(1, searchResults.getEntries().size());

	}

	@Test
	public void testA28CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A28
		Message exampleA28 = Hl7Helper.loadHl7Message("/example-messages/hl7v2/A28-2-mod.txt");

		// Post A28
		testMessageCanBePostedAndAcceptedByOink(exampleA28);

		// Search for Patient
		IGenericClient client = ITSupport.buildHapiClientForProxy(proxyProps);

		Bundle searchResults = client
				.search()
				.forResource(ca.uhn.fhir.model.dstu.resource.Patient.class)
				.where(ca.uhn.fhir.model.dstu.resource.Patient.IDENTIFIER
						.exactly().identifier("6509874369"))
				.and(ca.uhn.fhir.model.dstu.resource.Patient.GIVEN.matches()
						.value("RANDELL")).execute();

		assertEquals(1, searchResults.getEntries().size());

	}

	@Test
	public void testA31CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A31
		Message exampleA31 = Hl7Helper.loadHl7Message("/example-messages/hl7v2/A31-2-mod.txt");

		// Post A31
		testMessageCanBePostedAndAcceptedByOink(exampleA31);

		// Search for Patient
		IGenericClient client = ITSupport.buildHapiClientForProxy(proxyProps);

		Bundle searchResults = client
				.search()
				.forResource(ca.uhn.fhir.model.dstu.resource.Patient.class)
				.where(ca.uhn.fhir.model.dstu.resource.Patient.IDENTIFIER
						.exactly().identifier("4148734654"))
				.and(ca.uhn.fhir.model.dstu.resource.Patient.GIVEN.matches()
						.value("RICHIE")).execute();

		assertEquals(1, searchResults.getEntries().size());

	}

	public void testMessageCanBePostedAndAcceptedByOink(Message m)
			throws Exception {
		// Send message and get ACK response
		HapiContext context = new DefaultHapiContext();
		Connection hl7v2Conn = context.newClient(
				(String) hl7Props.get("hl7v2.host"),
				(Integer) Integer.parseInt(hl7Props.getProperty("hl7v2.port")),
				false);
		Initiator initiator = hl7v2Conn.getInitiator();
		ACK response = (ACK) initiator.sendAndReceive(m);
		context.close();

		assertEquals("AA", response.getMSA().getAcknowledgementCode()
				.getValue());
	}

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
				keepRuntimeFolder(),
				// Don't bother with local console output as it just ends up
				// cluttering the logs
				configureConsole().ignoreLocalConsole(),
				// Force the log level to INFO so we have more details during
				// the test. It defaults to WARN.
				//logLevel(LogLevel.DEBUG),
				// Provision the example feature exercised by this test
				features(oinkFeaturesRepo, "oink-adapter-hl7v2"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.hl7v2.cfg",
						ITSupport.getPropertyFileBySystemProperty("it.hl7v2.config")),
				features(oinkFeaturesRepo, "oink-adapter-proxy"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.proxy.cfg",
						ITSupport.getPropertyFileBySystemProperty("it.proxy.config")),
				replaceConfigurationFile("etc/org.ops4j.pax.logging.cfg",
								new File("src/test/resources/log4j.properties")),						

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
