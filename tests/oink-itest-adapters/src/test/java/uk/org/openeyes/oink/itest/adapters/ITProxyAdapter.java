package uk.org.openeyes.oink.itest.adapters;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.Parser;
import org.hl7.fhir.instance.model.Patient;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExamServer;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.proxy.test.support.RabbitClient;

/*
 * Tests the behaviour of the proxy adapter when deployed on the custom Karaf distro
 */
public class ITProxyAdapter {

	@Rule
	public PaxExamServer exam = new PaxExamServer();

	private static Properties props;
	private static final Logger log = LoggerFactory.getLogger(ITProxyAdapter.class);

	@BeforeClass
	public static void setUp() throws IOException {
		props = new Properties();
		InputStream is = ITProxyAdapter.class
				.getResourceAsStream("/proxy.properties");
		props.load(is);
	}

	@Test
	public void testPatientQuery() throws Exception {

		// Even though the bundles have started, the spring dm contexts may not
		// be started yet so we must wait
		Thread.sleep(10000);

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
	public void testCreateDeletePatient() throws Exception {
		
		Thread.sleep(10000);

		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Patient");
		req.setMethod(HttpMethod.POST);

		InputStream is = ITProxyAdapter.class
				.getResourceAsStream("/fhir/Patient.json");
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
		log.info("Posted to "+locationHeader);		

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
				// configureConsole().ignoreLocalConsole(),
				// Force the log level to INFO so we have more details during
				// the test. It defaults to WARN.
				logLevel(LogLevel.INFO),
				// Provision the example feature exercised by this test
				features(oinkFeaturesRepo, "oink-adapter-proxy"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.proxy.cfg",
						new File("src/test/resources/proxy.properties")),

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
