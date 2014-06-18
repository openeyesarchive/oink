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

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.Parser;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.PaxExamRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.it.ITSupport;
import uk.org.openeyes.oink.test.RabbitClient;

/*
 * Tests the behaviour of the proxy adapter when deployed on the custom Karaf distro
 */
public class ITProxyAdapter {

	private static Properties props;
	private static final Logger log = LoggerFactory
			.getLogger(ITProxyAdapter.class);

	private static TestContainer examContainer;

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {

		props = ITSupport.getPropertiesBySystemProperty("it.proxy.config");

		// Start Pax Exam
		ExamSystem system = PaxExamRuntime.createServerSystem(config());
		examContainer = PaxExamRuntime.createContainer(system);
		examContainer.start();

		// TODO Fix - For some reason a large wait is required
		Thread.sleep(120000);
	}

	@AfterClass
	public static void tearDown() {
		examContainer.stop();
	}

	@Test
	public void testOrganizationRead() throws Exception {

		// http://192.168.1.100:80/api/Organization/prac-1?_profile=http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Organization/prac-1");
		req.setParameters("_profile=http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice");
		req.setMethod(HttpMethod.GET);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(200, resp.getStatus());
		assertNotNull(resp.getBody());
		assertNotNull(resp.getBody().getResource());
		Organization org = (Organization) resp.getBody().getResource();
		assertEquals("F001", org.getIdentifier().get(0).getValueSimple());

	}

	@Test
	public void testNoOrganizationSearchResults() throws Exception {
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Organization");
		req.setParameters("_profile=http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice&identifier=XXXXX");
		req.setMethod(HttpMethod.GET);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(200, resp.getStatus());
		assertNotNull(resp.getBody());
		assertNotNull(resp.getBody().getBundle());
		assertEquals(0, resp.getBody().getBundle().getEntryList().size());
	}

	@Test
	public void testPatientSearchResults() throws Exception {
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Patient");
		req.setMethod(HttpMethod.GET);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(200, resp.getStatus());
		assertNotNull(resp.getBody());
		assertNotNull(resp.getBody().getBundle());
		assertNotEquals(0, resp.getBody().getBundle().getEntryList().size());
	}

	
	@Test
	public void testOrganizationCreateUpdateAndDelete() throws Exception {

		// CREATE
		// http://192.168.1.100:80/api/Organization?_profile=http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Organization");
		req.setMethod(HttpMethod.POST);
		req.addProfile("http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice");
		InputStream is = ITProxyAdapter.class
				.getResourceAsStream("/example-messages/fhir/organization.json");
		Parser parser = new JsonParser();
		Organization p = (Organization) parser.parse(is);
		FhirBody body = new FhirBody(p);
		req.setBody(body);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(201, resp.getStatus());
		String locationHeader = resp.getLocationHeader();
		assertNotNull(locationHeader);
		assertFalse(locationHeader.isEmpty());
		log.info("Posted to " + locationHeader);

		// See if Patient exists
		DefaultHttpClient httpClient = new DefaultHttpClient();

		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials("admin", "admin"));

		HttpGet httpGet = new HttpGet(locationHeader);
		httpGet.setHeader("Accept", "application/fhir+json");
		HttpResponse response1 = httpClient.execute(httpGet);
		assertEquals(200, response1.getStatusLine().getStatusCode());

		// UPDATE
		String id = getIdFromLocationHeader("Organization", locationHeader);
		OINKRequestMessage updateRequest = new OINKRequestMessage();
		updateRequest.setResourcePath("/Organization/" + id);
		updateRequest.setMethod(HttpMethod.PUT);
		updateRequest
				.addProfile("http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice");
		p.getTelecom().get(0).setValueSimple("0222 222 2222");
		updateRequest.setBody(new FhirBody(p));

		OINKResponseMessage updateResponse = client.sendAndRecieve(
				updateRequest, props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));
		assertEquals(200, updateResponse.getStatus());

		// DELETE
		OINKRequestMessage deleteRequest = new OINKRequestMessage();
		deleteRequest.setResourcePath("/Organization/" + id);
		deleteRequest.setMethod(HttpMethod.DELETE);
		deleteRequest
				.addProfile("http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice");

		OINKResponseMessage deleteResponse = client.sendAndRecieve(
				deleteRequest, props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));
		assertEquals(204, deleteResponse.getStatus());

	}

	public static String getIdFromLocationHeader(String resource,
			String location) {
		String patternString = ".*/" + resource + "/([^/]*)(/.*)?";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(location);
		matcher.find();
		return matcher.group(1);
	}

	public void testPractitionerRead() throws Exception {

		// http://192.168.1.100:80/api/Organization/prac-1?_profile=http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Practitioner/gp-1");
		req.addProfile("http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp");
		req.setMethod(HttpMethod.GET);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(200, resp.getStatus());
		assertNotNull(resp.getBody());
		assertNotNull(resp.getBody().getResource());
		Practitioner org = (Practitioner) resp.getBody().getResource();
		assertEquals("MNOP", org.getIdentifier().get(0).getValueSimple());

	}

	@Test
	public void testPractitionerCreateUpdateAndDelete() throws Exception {

		// CREATE
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Practitioner");
		req.setMethod(HttpMethod.POST);
		req.addProfile("http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp");
		InputStream is = ITProxyAdapter.class
				.getResourceAsStream("/example-messages/fhir/practitioner2.json");
		Parser parser = new JsonParser();
		Practitioner p = (Practitioner) parser.parse(is);
		FhirBody body = new FhirBody(p);
		req.setBody(body);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(201, resp.getStatus());
		String locationHeader = resp.getLocationHeader();
		assertNotNull(locationHeader);
		assertFalse(locationHeader.isEmpty());
		log.info("Posted to " + locationHeader);

		// See if Patient exists
		DefaultHttpClient httpClient = new DefaultHttpClient();

		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials("admin", "admin"));

		HttpGet httpGet = new HttpGet(locationHeader);
		httpGet.setHeader("Accept", "application/fhir+json");
		HttpResponse response1 = httpClient.execute(httpGet);
		assertEquals(200, response1.getStatusLine().getStatusCode());

		// UPDATE
		String id = getIdFromLocationHeader("Practitioner", locationHeader);
		OINKRequestMessage updateRequest = new OINKRequestMessage();
		updateRequest.setResourcePath("/Practitioner/" + id);
		updateRequest.setMethod(HttpMethod.PUT);
		updateRequest
				.addProfile("http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp");
		p.getTelecom().get(0).setValueSimple("0222 222 2222");
		updateRequest.setBody(new FhirBody(p));

		OINKResponseMessage updateResponse = client.sendAndRecieve(
				updateRequest, props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));
		assertEquals(200, updateResponse.getStatus());

		// DELETE
		OINKRequestMessage deleteRequest = new OINKRequestMessage();
		deleteRequest.setResourcePath("/Practitioner/" + id);
		deleteRequest.setMethod(HttpMethod.DELETE);
		deleteRequest
				.addProfile("http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp");

		OINKResponseMessage deleteResponse = client.sendAndRecieve(
				deleteRequest, props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));
		assertEquals(204, deleteResponse.getStatus());

	}

	@Test
	public void testPatientQuery() throws Exception {
		// http://192.168.1.100/api/Patient?identifier=1007913&_format=json
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Patient");
		req.setParameters("identifier=1007913");
		req.setMethod(HttpMethod.GET);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(200, resp.getStatus());
	}

	@Test
	public void testGetPatient() throws Exception {

		Thread.sleep(10000);

		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Patient/17885");
		req.setMethod(HttpMethod.GET);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(200, resp.getStatus());

	}

	@Test
	public void testCreatePatient() throws Exception {

		Thread.sleep(10000);

		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Patient");
		req.setMethod(HttpMethod.POST);

		InputStream is = ITProxyAdapter.class
				.getResourceAsStream("/example-messages/fhir/patient2.json");
		Parser parser = new JsonParser();
		Patient p = (Patient) parser.parse(is);

		FhirBody body = new FhirBody(p);
		req.setBody(body);

		RabbitClient client = new RabbitClient(
				props.getProperty("rabbit.host"), Integer.parseInt(props
						.getProperty("rabbit.port")),
				props.getProperty("rabbit.vhost"),
				props.getProperty("rabbit.username"),
				props.getProperty("rabbit.password"));

		OINKResponseMessage resp = client.sendAndRecieve(req,
				props.getProperty("rabbit.routingKey"),
				props.getProperty("rabbit.defaultExchange"));

		assertEquals(201, resp.getStatus());

		String locationHeader = resp.getLocationHeader();
		assertNotNull(locationHeader);
		assertFalse(locationHeader.isEmpty());
		log.info("Posted to " + locationHeader);

		// Check exists on server
		// See if Patient exists
		DefaultHttpClient httpClient = new DefaultHttpClient();

		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials("admin", "admin"));

		HttpGet httpGet = new HttpGet(locationHeader);
		httpGet.setHeader("Accept", "application/fhir+json");
		HttpResponse response1 = httpClient.execute(httpGet);
		assertEquals(200, response1.getStatusLine().getStatusCode());
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
				// Don't bother with local console output as it just ends up
				// cluttering the logs
				// configureConsole().ignoreLocalConsole(),
				// Force the log level to INFO so we have more details during
				// the test. It defaults to WARN.
				// Provision the example feature exercised by this test
				replaceConfigurationFile(
						"etc/uk.org.openeyes.oink.proxy.cfg",
						ITSupport.getPropertyFileBySystemProperty("it.proxy.config")),
				features(oinkFeaturesRepo, "oink-adapter-proxy"),
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
