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
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.ResourceType;
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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.dstu.resource.Conformance;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.rest.client.HttpBasicAuthInterceptor;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.IRestfulClientFactory;
import uk.org.openeyes.oink.fhir.BundleParser;
import uk.org.openeyes.oink.it.ITSupport;

/**
 * 
 * Tests the intercompability of the standard Facade and the standard
 * Proxy adapter running on the same Karaf container.
 * 
 * The proxy adapter is bound to an OpenEyes instance
 * 
 * @author Oliver Wilkie
 */
public class ITFacadeToProxy {


	private static Properties facadeProps;
	private static Properties proxyProps;
	
	private static TestContainer examContainer;

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {

		facadeProps = ITSupport.getPropertiesBySystemProperty("it.facadeToProxy.config");

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
	public void testCanGetMetadataOfOpenEyes() {
		
		String facadeUri = (String) facadeProps.get("facade.uri");

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
		
		IGenericClient client = clientFactory.newGenericClient(facadeUri);		
		
		Conformance c = client.conformance();
		
		assertNotNull(c);
		
	}
	
	@Test
	public void testGetPatients() {
		String facadeUri = (String) facadeProps.get("facade.uri");

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
		
		IGenericClient client = clientFactory.newGenericClient(facadeUri);		
		
		Bundle response = client.search().forResource(Patient.class).execute();
		
		assertNotNull(response);
		assertNotEquals(0, response.getEntries().size());
		
	}
	
	@Test
	public void testGetPractitioners() throws Exception {
		String facadeUri = (String) facadeProps.get("facade.uri");
		
		URIBuilder builder = new URIBuilder(facadeUri);
		URI uri = builder.setPath(builder.getPath()+"/Practitioner").setParameter("_profile", "http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp").build();
				
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(uri);
		httpGet.addHeader("Accept", "application/json+fhir; charset=UTF-8");
		CloseableHttpResponse response1 = httpclient.execute(httpGet);

		assertEquals(200, response1.getStatusLine().getStatusCode());
		String json = null;
		try {
		    HttpEntity entity1 = response1.getEntity();
		    json = EntityUtils.toString(entity1);
		} finally {
		    response1.close();
		}
		
		assertNotNull(json);
	
		BundleParser conv = new BundleParser();
		AtomFeed response = conv.fromJsonOrXml(json);
		
		assertNotEquals(0, response.getEntryList().size());	
	}
	
	@Test
	public void testCreateAndDeletePractitioners() throws Exception {
		String facadeUri = (String) facadeProps.get("facade.uri");
		
		URIBuilder builder = new URIBuilder(facadeUri);
		URI uri = builder.setPath(builder.getPath()+"/Practitioner").setParameter("_profile", "http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp").build();
		
		System.out.println(uri.toString());
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader("Accept", "application/json+fhir; charset=UTF-8");
		httpPost.addHeader("Category","http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp; scheme=\"http://hl7.org/fhir/tag/profile\"; label=\"\"");
		httpPost.addHeader("Content-Type","application/json+fhir");
		InputStream is = getClass().getResourceAsStream("/example-messages/fhir/practitioner.json");
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String theString = writer.toString();
		
		StringEntity isEntity = new StringEntity(theString); 
				
		httpPost.setEntity(isEntity);
		
		CloseableHttpResponse response1 = httpclient.execute(httpPost);

		assertEquals(201, response1.getStatusLine().getStatusCode());
		
		// Note location header is the real end-server location not the facade
		// e.g. http://192.168.1.100/api/Practitioner/gp-4/_history/1401366763
		String locationHeader = response1.getHeaders("Location")[0].getValue();
		assertNotNull(locationHeader);
		
		String resourceId = extractResourceIdFromUri(locationHeader);
		assertNotNull(resourceId);
		
		URIBuilder builder2 = new URIBuilder(facadeUri);
		URI uri2 = builder2.setPath(builder2.getPath()+"/Practitioner/"+resourceId).setParameter("_profile", "http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp").build();
		
		HttpDelete httpDelete = new HttpDelete(uri2);
		CloseableHttpResponse response2 = httpclient.execute(httpDelete);
		assertEquals(204, response2.getStatusLine().getStatusCode());
	}
	
	private String extractResourceIdFromUri(String locationUri) {
		
		String[] parts = locationUri.split("/");
		for (int i=0; i<parts.length-1; i++) {
			try {
				ResourceType res = ResourceType.valueOf(parts[i]);
				return parts[i+1];
			} catch (Exception ex) {
				continue;
			}
		}
		return null;
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
				features(oinkFeaturesRepo, "oink-adapter-facade"),
				replaceConfigurationFile("etc/uk.org.openeyes.oink.facade.cfg",
						ITSupport.getPropertyFileBySystemProperty("it.facadeToProxy.config")),
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
