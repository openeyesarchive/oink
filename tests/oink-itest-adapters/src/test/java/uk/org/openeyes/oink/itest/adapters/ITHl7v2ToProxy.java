package uk.org.openeyes.oink.itest.adapters;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExamServer;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.util.PathUtils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.rest.client.HttpBasicAuthInterceptor;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.IRestfulClientFactory;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ACK;
import uk.org.openeyes.oink.hl7v2.test.Hl7ITSupport;

/**
 * 
 * Tests the intercompability of the standard HL7v2 Adapter and the standard
 * Proxy adapter running on the same Karaf container.
 * 
 * @author Oliver Wilkie
 */
public class ITHl7v2ToProxy {

	@Rule
	public PaxExamServer exam = new PaxExamServer();

	private static Properties hl7Props;
	private static Properties proxyProps;

	@BeforeClass
	public static void setUp() throws IOException {

		hl7Props = new Properties();
		InputStream hl7PropsIs = ITHl7v2ToProxy.class
				.getResourceAsStream("/hl7v2.properties");
		hl7Props.load(hl7PropsIs);

		proxyProps = new Properties();
		InputStream proxyPropsIs = ITHl7v2ToProxy.class
				.getResourceAsStream("/proxy.properties");
		proxyProps.load(proxyPropsIs);
	}

	@Before
	public void before() throws InterruptedException {
		// Even though the bundles have started, the spring dm contexts may not
		// be started yet so we must wait
		Thread.sleep(10000);
	}

	@Test
	public void testA01CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A01
		Message exampleA01 = Hl7ITSupport.loadHl7Message("/hl7v2/A01-mod.txt");

		// Post A01
		testMessageCanBePostedAndAcceptedByOink(exampleA01);

		// Search for Patient
		IGenericClient client = buildHapiClient(proxyProps);

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
		Message exampleA05 = Hl7ITSupport.loadHl7Message("/hl7v2/A05.txt");

		// Post A01
		testMessageCanBePostedAndAcceptedByOink(exampleA05);

		// Search for Patient
		IGenericClient client = buildHapiClient(proxyProps);

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
		Message exampleA28 = Hl7ITSupport.loadHl7Message("/hl7v2/A28-2.txt");

		// Post A28
		testMessageCanBePostedAndAcceptedByOink(exampleA28);

		// Search for Patient
		IGenericClient client = buildHapiClient(proxyProps);

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
		Message exampleA31 = Hl7ITSupport.loadHl7Message("/hl7v2/A31-2.txt");

		// Post A31
		testMessageCanBePostedAndAcceptedByOink(exampleA31);

		// Search for Patient
		IGenericClient client = buildHapiClient(proxyProps);

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
		Properties hl7Props = new Properties();
		InputStream hl7PropsIs = ITHl7v2ToProxy.class
				.getResourceAsStream("/hl7v2.properties");
		hl7Props.load(hl7PropsIs);

		Properties proxyProps = new Properties();
		InputStream proxyPropsIs = ITHl7v2ToProxy.class
				.getResourceAsStream("/proxy.properties");
		proxyProps.load(proxyPropsIs);

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

	public static IGenericClient buildHapiClient(Properties proxyProps) {
		// See if Patient exists
		String proxyUri = (String) proxyProps.get("proxy.uri");

		// Create a context and get the client factory so it can be configured
		FhirContext ctx = new FhirContext();
		IRestfulClientFactory clientFactory = ctx.getRestfulClientFactory();

		// Create an HTTP Client Builder
		HttpClientBuilder builder = HttpClientBuilder.create();

		// This interceptor adds HTTP username/password to every request
		String username = (String) proxyProps.get("proxy.username");
		String password = (String) proxyProps.get("proxy.password");
		builder.addInterceptorFirst(new HttpBasicAuthInterceptor(username,
				password));
		
		builder.addInterceptorFirst(new HttpRequestInterceptor() {
			
			@Override
			public void process(HttpRequest req, HttpContext context)
					throws HttpException, IOException {
				req.addHeader("Accept", "application/json+fhir; charset=UTF-8");
			}
		});

		// Use the new HTTP client builder
		clientFactory.setHttpClient(builder.build());
		
		IGenericClient client = clientFactory.newGenericClient("http://"
				+ proxyUri);
		
		

		return client;
	}

	@Configuration
	public Option[] config() {
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
						new File("src/test/resources/hl7v2.properties")),
				features(oinkFeaturesRepo, "oink-adapter-proxy"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.proxy.cfg",
						new File("src/test/resources/proxy.properties")),
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
