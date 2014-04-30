package uk.org.openeyes.oink.facade;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.eclipse.jetty.jndi.local.localContextRoot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:camel-context-test.xml" })
public class TestFacadeRoute {

	private static Properties testProperties;

	@BeforeClass
	public static void setUp() throws IOException {
		testProperties = new Properties();
		InputStream is = TestFacadeRoute.class
				.getResourceAsStream("/facade-test.properties");
		testProperties.load(is);
	}

	@Test
	@DirtiesContext
	public void testRequestFailsOnMissingAuthenticationHeader()
			throws HttpException, IOException {

		// Prepare request
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(
				testProperties.getProperty("facade.uri") + "/Patient");

		client.executeMethod(method);
		byte[] responseBody = method.getResponseBody();
		method.releaseConnection();

		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, method.getStatusCode());
	}

	@Test
	@DirtiesContext
	public void testRequestFailsOnInvalidCredentials() throws HttpException,
			IOException {

		// Prepare request
		HttpClient client = new HttpClient();

		HttpMethod method = new GetMethod(
				testProperties.getProperty("facade.uri") + "/Patient");
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("wrongusernameandformat", testProperties
				.getProperty("testUser.password"));
		
		method.addRequestHeader("Authorization", BasicScheme.authenticate(creds,"US-ASCII"));
		client.executeMethod(method);
		byte[] responseBody = method.getResponseBody();
		method.releaseConnection();

		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, method.getStatusCode());
	}

	@Test
	@DirtiesContext
	public void testAuthenticationCanSucceed() throws HttpException,
			IOException {

		// Prepare request
		
		
		HttpClient client = new HttpClient();

		HttpMethod method = new GetMethod(
				testProperties.getProperty("facade.uri") + "/Patient");
		
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(testProperties
				.getProperty("testUser.username"), testProperties
				.getProperty("testUser.password"));
		
		method.addRequestHeader("Authorization", BasicScheme.authenticate(creds,"US-ASCII"));
				
		client.executeMethod(method);
		byte[] responseBody = method.getResponseBody();
		method.releaseConnection();

		Assert.assertNotEquals(HttpStatus.SC_UNAUTHORIZED, method.getStatusCode());
	}

}
