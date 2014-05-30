package uk.org.openeyes.oink.itest.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.instance.model.AtomFeed;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import uk.org.openeyes.oink.fhir.FhirConverter;
import uk.org.openeyes.oink.test.Hl7Helper;
import uk.org.openeyes.oink.test.Hl7Server;

public class ITFacadeToHl7v2 {
	
	private static Properties hl7Props;
	private static Properties facadeProps;
	
	private static TestContainer examContainer;

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {

		hl7Props = new Properties();
		InputStream hl7PropsIs = ITFacadeToHl7v2.class
				.getResourceAsStream("/hl7v2.properties");
		hl7Props.load(hl7PropsIs);

		facadeProps = new Properties();
		InputStream proxyPropsIs = ITFacadeToHl7v2.class
				.getResourceAsStream("/facade-toHl7.properties");
		facadeProps.load(proxyPropsIs);
		
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
	
	@Test
	public void testPatientQueryIsPossibleUsingMockedHl7Server() throws Exception {
		
		// Mock an HL7 Server
		Hl7Server hl7Server = new Hl7Server(5678, false);
		
		final Message searchResults = Hl7Helper.loadHl7Message("/example-messages/hl7v2/ADR-A19.txt");
		hl7Server.setMessageHandler("QRY", "A19", new ReceivingApplication() {
			
			@Override
			public Message processMessage(Message in, Map<String, Object> metadata)
					throws ReceivingApplicationException, HL7Exception {
				// Always return search results
				return searchResults;
			}
			
			@Override
			public boolean canProcess(Message message) {
				return true;
			}
		});
		
		hl7Server.start();
		
		// Make a Patient Query
		URIBuilder builder = new URIBuilder(facadeProps.getProperty("facade.uri")+"/Patient");
		builder.addParameter("identifier", "NHS|123456");
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(builder.build());
		httpGet.addHeader("Accept", "application/json+fhir; charset=UTF-8");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);

		// Check results
		assertEquals(200, response1.getStatusLine().getStatusCode());
		String json = null;
		try {
		    HttpEntity entity1 = response1.getEntity();
		    json = EntityUtils.toString(entity1);
		} finally {
		    response1.close();
			hl7Server.stop();
		}
		
		assertNotNull(json);
	
		FhirConverter conv = new FhirConverter();
		AtomFeed response = conv.fromJsonOrXml(json);
		
		assertNotEquals(0, response.getEntryList().size());	
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
						new File("../oink-itest-shared/src/main/resources/hl7v2.properties")),
				features(oinkFeaturesRepo, "oink-adapter-facade"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.facade.cfg",
						new File("src/test/resources/facade-toHl7.properties")),
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
