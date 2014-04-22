package uk.org.openeyes.oink.example.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;


@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TestFacadeRoute {
	
	@Autowired
	protected CamelContext camelContext;
	
	private static Properties props;
	
	@BeforeClass
	public static void before() throws IOException {
		props = new Properties();
		InputStream in = TestFacadeRoute.class.getResourceAsStream("/uk/org/openeyes/oink/example/proxy/facade.properties");
		props.load(in);
		in.close();
	}
	
	
	@Test
	public void testFacadeDoesAValidRoundTrip() throws IOException {
		// Prepare Request
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://localhost:8899/oink/Patient");
		
		String encoding = Base64.encodeBase64String("bob@ucc1:bob".getBytes());
		httpPost.addHeader("Authorization", "Basic " + encoding);
		
		String samplePatient = IOUtils.toString(this.getClass().getResourceAsStream("/patient.json"));
		HttpEntity entity = new StringEntity(samplePatient, ContentType.create("application/json+fhir"));
		httpPost.setEntity(entity);
		
		// Post request
		HttpResponse r = httpClient.execute(httpPost);
		
		// Assert returned error code is 200
		Assert.assertEquals(200, r.getStatusLine().getStatusCode());	
	}
	
	@Test
	public void testFacadeRejectsUnauthenticatedUserWithErrorCode403() throws ClientProtocolException, IOException {
		// Prepare Request
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://localhost:8899/oink/Patient");
		
		// Post request
		HttpResponse r = httpClient.execute(httpPost);
		
		// Assert returned error code is 403
		Assert.assertEquals(403, r.getStatusLine().getStatusCode());		
	}
	
	@Test
	public void testFacadeRejectsInvalidRequestBodiesWithErrorCode400() throws IOException {
		// Prepare Request
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://localhost:8899/oink/Patient");
		
		String encoding = Base64.encodeBase64String("bob@ucc1:bob".getBytes());
		httpPost.addHeader("Authorization", "Basic " + encoding);
		
		String samplePatient = "{adfmasdkjasiopdjkaodihap;l}";
		HttpEntity entity = new StringEntity(samplePatient, ContentType.create("application/json+fhir"));
		httpPost.setEntity(entity);
		
		// Post request
		HttpResponse r = httpClient.execute(httpPost);
		
		// Assert returned error code is 400
		Assert.assertEquals(400, r.getStatusLine().getStatusCode());		
	}
	
	@Test
	public void testFacadeRejectsRequestsWithNoMatchingMappingsWith404() throws ClientProtocolException, IOException {
		// Prepare Request
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://localhost:8899/oink/Alien");
		
		String encoding = Base64.encodeBase64String("bob@ucc1:bob".getBytes());
		httpPost.addHeader("Authorization", "Basic " + encoding);
		
		String samplePatient = IOUtils.toString(this.getClass().getResourceAsStream("/patient.json"));
		HttpEntity entity = new StringEntity(samplePatient, ContentType.create("application/json+fhir"));
		httpPost.setEntity(entity);
		
		// Post request
		HttpResponse r = httpClient.execute(httpPost);
		
		// Assert returned error code is 404
		Assert.assertEquals(404, r.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testFacadeHandlesTimeoutOnOtherOinkSystemWith504() throws IOException {
		// Prepare Request
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://localhost:8899/oink/Practitioner");
		
		String encoding = Base64.encodeBase64String("bob@ucc1:bob".getBytes());
		httpPost.addHeader("Authorization", "Basic " + encoding);
		
		String samplePatient = IOUtils.toString(this.getClass().getResourceAsStream("/patient.json"));
		HttpEntity entity = new StringEntity(samplePatient, ContentType.create("application/json+fhir"));
		httpPost.setEntity(entity);
		
		// Post request
		HttpResponse r = httpClient.execute(httpPost);
		
		// Assert returned error code is 504
		Assert.assertEquals(504, r.getStatusLine().getStatusCode());		
	}
	
	@Test
	public void testSendingInvalidContentTypeReturnsErrorCode400() throws IOException {
		// Prepare Request
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("http://localhost:8899/oink/Patient");
		
		String encoding = Base64.encodeBase64String("bob@ucc1:bob".getBytes());
		httpPost.addHeader("Authorization", "Basic " + encoding);
		
		String samplePatient = IOUtils.toString(this.getClass().getResourceAsStream("/patient.json"));
		HttpEntity entity = new StringEntity(samplePatient, ContentType.create("application/json+fhi"));
		httpPost.setEntity(entity);
		
		// Post request
		HttpResponse r = httpClient.execute(httpPost);
		
		// Assert returned error code is 400
		Assert.assertEquals(400, r.getStatusLine().getStatusCode());
	}

}
