package uk.org.openeyes.oink.facade;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.BasicScheme;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:camel-context-test.xml" })
public class ITFacadeRoute {

	private static Properties testProperties;

	private static ConnectionFactory factory;

	private final static String THIRD_PARTY_QUEUE_NAME = "siteB";
	
	private volatile AssertionError thirdPartyAssertionError; 

	@Autowired
	CamelContext camelCtx;

	@BeforeClass
	public static void setUp() throws IOException {
		// Load properties
		testProperties = new Properties();
		InputStream is = ITFacadeRoute.class
				.getResourceAsStream("/facade-test.properties");
		testProperties.load(is);

		// Prepare RabbitMQ Client
		factory = new ConnectionFactory();
		factory.setHost(testProperties.getProperty("rabbit.host"));
		factory.setPort(Integer.parseInt(testProperties
				.getProperty("rabbit.port")));
		factory.setUsername(testProperties.getProperty("rabbit.username"));
		factory.setPassword(testProperties.getProperty("rabbit.password"));
		factory.setVirtualHost(testProperties.getProperty("rabbit.vhost"));

	}
	
	@Before
	public void before() {
		thirdPartyAssertionError = null;
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
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				"wrongusernameandformat",
				testProperties.getProperty("testUser.password"));

		method.addRequestHeader("Authorization",
				BasicScheme.authenticate(creds, "US-ASCII"));
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

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				testProperties.getProperty("testUser.username"),
				testProperties.getProperty("testUser.password"));

		method.addRequestHeader("Authorization",
				BasicScheme.authenticate(creds, "US-ASCII"));

		client.executeMethod(method);
		byte[] responseBody = method.getResponseBody();
		method.releaseConnection();

		Assert.assertNotEquals(HttpStatus.SC_UNAUTHORIZED,
				method.getStatusCode());
	}
	
	@Test
	@DirtiesContext
	public void testSimplePatientPost() throws Exception {
		
		
		/*
		 * Set up Third Party Service
		 */		
		
		// Specify what the third party service should receive
		IncomingMessageVerifier v = new IncomingMessageVerifier() {
			@Override
			public void isValid(OINKRequestMessage incoming) {
				Assert.assertEquals(uk.org.openeyes.oink.domain.HttpMethod.POST, incoming.getMethod());
				Assert.assertEquals("/Patient", incoming.getResourcePath());

				try {
					// FHIR Resource Impl Team haven't implemented equals() yet
					JsonComposer composer = new JsonComposer();
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					composer.compose(os, incoming.getBody().getResource(), false);
					String receivedJson = os.toString();
					String expectedJson = IOUtils.toString(this.getClass().getResourceAsStream("/patient.json"));
					Assert.assertEquals(expectedJson, receivedJson);
				} catch (Exception e) {
					Assert.assertTrue(false);
				}
				
			}
		};
		
		// Specify what the third party service should return
		OINKResponseMessage mockResponse = new OINKResponseMessage();
		mockResponse.setStatus(201);
		mockResponse.setBody(null);

		// Start the third party service
		SimulatedThirdParty thirdp = new SimulatedThirdParty(v, mockResponse);
		thirdp.start();
		
		/*
		 * Make REST request
		 */

		// Prepare request
		HttpClient client = new HttpClient();

		PostMethod method = new PostMethod(
				testProperties.getProperty("facade.uri") + "/Patient");

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				testProperties.getProperty("testUser.username"),
				testProperties.getProperty("testUser.password"));

		method.addRequestHeader("Authorization",
				BasicScheme.authenticate(creds, "US-ASCII"));
		
		method.addRequestHeader("Content-Type", "application/json+fhir");

		InputStream is = this.getClass().getResourceAsStream("/patient.json");
		method.setRequestEntity(new InputStreamRequestEntity(is));
		client.executeMethod(method);
		thirdp.close();
		
		/*
		 * Process REST response
		 */
		byte[] responseBody = method.getResponseBody();
		String s = method.getResponseBodyAsString();
		int responseCode = method.getStatusCode();
		method.releaseConnection();
		
		if (thirdPartyAssertionError != null) {
			throw thirdPartyAssertionError;
		}
		
		
		Assert.assertEquals(HttpStatus.SC_CREATED, responseCode);
		Assert.assertNull(method.getResponseHeader("Content-Type"));
		Assert.assertArrayEquals(responseBody, new byte[]{});		
		
	}

	@Test
	@DirtiesContext
	public void testSimplePatientGet() throws Exception {

		/*
		 * Set up Third Party Service
		 */
		
		// Specify what the third party service should receive
		IncomingMessageVerifier v = new IncomingMessageVerifier() {
			@Override
			public void isValid(OINKRequestMessage incoming) {
				Assert.assertEquals(uk.org.openeyes.oink.domain.HttpMethod.GET, incoming.getMethod());
				Assert.assertEquals("/Patient/2342452", incoming.getResourcePath());
				Assert.assertNull(incoming.getBody());
			}
		};
		
		// Specify what the third party service should return
		OINKResponseMessage mockResponse = new OINKResponseMessage();
		mockResponse.setStatus(200);
		mockResponse.setBody(buildFhirBodyFromResource("/patient.json"));

		// Start the third party service
		SimulatedThirdParty thirdp = new SimulatedThirdParty(v, mockResponse);
		thirdp.start();
		
		/*
		 * Make REST request
		 */

		// Prepare request
		HttpClient client = new HttpClient();

		HttpMethod method = new GetMethod(
				testProperties.getProperty("facade.uri") + "/Patient/2342452");

		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				testProperties.getProperty("testUser.username"),
				testProperties.getProperty("testUser.password"));

		method.addRequestHeader("Authorization",
				BasicScheme.authenticate(creds, "US-ASCII"));

		client.executeMethod(method);
		thirdp.close();

		/*
		 * Process REST response
		 */
		byte[] responseBody = method.getResponseBody();
		String responseJson = new String(responseBody);
		int responseCode = method.getStatusCode();
		String responseContentType = method.getResponseHeader("Content-Type").getValue();
		method.releaseConnection();
		
		if (thirdPartyAssertionError != null) {
			throw thirdPartyAssertionError;
		}
		
		Assert.assertEquals(HttpStatus.SC_OK, responseCode);
		Assert.assertEquals("application/json+fhir", responseContentType);
		Assert.assertEquals(IOUtils.toString(this.getClass().getResourceAsStream("/patient.json"),"UTF-8"), responseJson);
	}
	
	private static FhirBody buildFhirBodyFromResource(String resourcePath) throws Exception {
		InputStream is = ITFacadeRoute.class.getResourceAsStream(resourcePath);
		FhirBody body = null;
		JsonParser parser = new JsonParser();
		ResourceOrFeed res = parser.parseGeneral(is);
		if (res.getFeed() != null) {
			body = new FhirBody(res.getFeed());
		} else if (res.getResource() != null) {
			body = new FhirBody(res.getResource());
		}
		return body;
	}
	
	private interface IncomingMessageVerifier {
		public void isValid(OINKRequestMessage incoming);
	}

	private class SimulatedThirdParty extends Thread {
		
		private IncomingMessageVerifier verifier;
		private OINKResponseMessage messageToReplyWith;
		
		public SimulatedThirdParty(IncomingMessageVerifier verifier, OINKResponseMessage messageToReplyWith) {
			super();
			this.verifier = verifier;
			this.messageToReplyWith = messageToReplyWith;
		}

		boolean isRunning = true;

		@Override
		public void run() {
			try {
				simulateThirdParty();
			} catch (AssertionError e) {
				thirdPartyAssertionError = e;
			} catch (ShutdownSignalException e) {
			} catch (ConsumerCancelledException e) {
			} catch (IOException e) {
			} catch (InterruptedException e) {
			}
		}

		public void close() {
			isRunning = false;
		}

		public void simulateThirdParty() throws IOException,
				ShutdownSignalException, ConsumerCancelledException,
				InterruptedException {
			// Build consumer
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			channel.queueDeclare(THIRD_PARTY_QUEUE_NAME, false, false, true,
					null);
			channel.queueBind(THIRD_PARTY_QUEUE_NAME,
					testProperties.getProperty("rabbit.defaultExchange"),
					THIRD_PARTY_QUEUE_NAME);
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(THIRD_PARTY_QUEUE_NAME, true, consumer);

			while (isRunning) {
				// Get delivery (timeout if necessary)
				QueueingConsumer.Delivery delivery = consumer
						.nextDelivery(5000);
				if (delivery != null) {
					isRunning = false;
				} else {
					continue;
				}

				BasicProperties props = delivery.getProperties();

				// Extract request message
				OINKRequestMessage message = camelCtx
						.getTypeConverter()
						.convertTo(OINKRequestMessage.class, delivery.getBody());
				
				// Check is valid
				verifier.isValid(message);

				// Prepare an empty response
				com.rabbitmq.client.AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
						.correlationId(props.getCorrelationId()).build();

				byte[] responseBody = camelCtx.getTypeConverter().convertTo(
						byte[].class, messageToReplyWith);

				channel.basicPublish(
						testProperties.getProperty("rabbit.defaultExchange"),
						props.getReplyTo(), replyProps, responseBody);
			}
			connection.close();
		}

	}

}
