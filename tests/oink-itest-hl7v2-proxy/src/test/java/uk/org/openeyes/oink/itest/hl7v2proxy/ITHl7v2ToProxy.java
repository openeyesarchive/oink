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
import java.io.InputStream;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.Parser;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;

import ca.uhn.hl7v2.model.Message;
import uk.org.openeyes.oink.hl7v2.test.Hl7ITSupport;
import uk.org.openeyes.oink.hl7v2.test.support.Hl7Client;

@RunWith(PaxExam.class)
public class ITHl7v2ToProxy {

	public void testA01CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A01
		Message exampleA01 = Hl7ITSupport.loadHl7Message("");

		// Send HL7v2 Client
		Hl7Client.send(exampleA01, "", 0);

		// Search for Patient in FHIR Server
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(
				"http://rabbit.com/fhir/Patient?identifier=NHS|121311131");
		CloseableHttpResponse response1 = httpClient.execute(httpGet);
		AtomFeed bundle = null;
		try {
			int statusCode = response1.getStatusLine().getStatusCode();
			HttpEntity entity1 = response1.getEntity();
			InputStream is1 = entity1.getContent();
			Parser parser = new JsonParser();
			bundle = parser.parseGeneral(is1).getFeed();
		} finally {
			response1.close();
		}
		assertNotNull(bundle);
		
		// Find patient
		Patient patient = null;
		String patientId = null;
		List<AtomEntry<? extends Resource>> list = bundle.getEntryList();
		outerLoop:
		for (AtomEntry<? extends Resource> entry : list) {
			String pid = entry.getId();
			Patient p = (Patient) entry.getResource();
			for (Identifier id : p.getIdentifier()) {
				if (id.getSystemSimple().equals("NHS") && id.getValueSimple().equals("9999999999")) {
					patient = p;
					patientId = pid;
					break outerLoop;
				}
			}
		}
		assertNotNull(patient);
		
		// Check details of Patient
		fail("Not fully implemented");

		// Delete Patient for future tests
		HttpDelete httpDelete = new HttpDelete(patientId);
		HttpResponse response2 = httpClient.execute(httpDelete);
		
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
				logLevel(LogLevel.INFO),
				features(
						"mvn:org.apache.karaf.features/spring/3.0.1/xml/features",
						"spring-dm"),
				// Provision the example feature exercised by this test
				features(oinkFeaturesRepo, "oink-adapter-hl7v2"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.hl7v2.cfg",
						new File("src/test/resources/hl7v2.properties")),
				features(oinkFeaturesRepo, "oink-adapter-proxy"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.proxy.cfg",
						new File("src/test/resources/proxy.properties")),
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
