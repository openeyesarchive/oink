/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import uk.org.openeyes.oink.fhir.BundleParser;
import uk.org.openeyes.oink.it.ITSupport;
import uk.org.openeyes.oink.test.Hl7TestUtils;
import uk.org.openeyes.oink.test.Hl7Server;

/**
 * 
 * This class tests the behaviour of using the facade with an HL7v2 Server. It
 * tests a user's ability to make FHIR REST requests which get converted to/from
 * HL7v2 messages by the HL7v2 adapter.
 * 
 */
public class ITFacadeToHl7v2 {

	private static Properties hl7Props;
	private static Properties facadeProps;
	
	private static final Logger log = LoggerFactory.getLogger(ITFacadeToHl7v2.class);

	private static TestContainer examContainer;

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {
		
		hl7Props = ITSupport.getPropertiesBySystemProperty("it.hl7v2.config");
		facadeProps = ITSupport.getPropertiesBySystemProperty("it.facadeToHl7v2.config");

		// Start Pax Exam
		ExamSystem system = PaxExamRuntime.createServerSystem(config());
		examContainer = PaxExamRuntime.createContainer(system);
		examContainer.start();

		// TODO Fix - For some reason a large wait is required
		Thread.sleep(45000);
	}

	@AfterClass
	public static void tearDown() {
		if (examContainer != null) {
			examContainer.stop();
		}
	}

	@Test
	public void testPatientQueryIsPossibleUsingMockedHl7Server()
			throws Exception {
		
		Thread.sleep(45000);

		// Mock an HL7 Server
		Hl7Server hl7Server = new Hl7Server(Integer.parseInt(hl7Props.getProperty("remote.port")), false);

		final Message searchResults = Hl7TestUtils
				.loadHl7Message("/example-messages/hl7v2/ADR-A19-mod.txt");
		hl7Server.setMessageHandler("QRY", "A19", new ReceivingApplication() {

			@Override
			public Message processMessage(Message in,
					Map<String, Object> metadata)
					throws ReceivingApplicationException, HL7Exception {
				// Always return search results
				log.debug("Returning search results");
				return searchResults;
			}

			@Override
			public boolean canProcess(Message message) {
				return true;
			}
		});

		hl7Server.start();

		// Make a Patient Query
		URIBuilder builder = new URIBuilder(
				facadeProps.getProperty("facade.uri") + "/Patient");
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

		BundleParser conv = new BundleParser();
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
				// logLevel(LogLevel.DEBUG),
				// Provision the example feature exercised by this test
				features(oinkFeaturesRepo, "oink-adapter-hl7v2"),
				replaceConfigurationFile(
						"etc/uk.org.openeyes.oink.hl7v2.cfg",
						ITSupport.getPropertyFileBySystemProperty("it.hl7v2.config")),
				features(oinkFeaturesRepo, "oink-adapter-facade"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.facade.cfg",
						ITSupport.getPropertyFileBySystemProperty("it.facadeToHl7v2.config")),
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
