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
package uk.org.openeyes.oink.proxy.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.commons.io.IOUtils;

import static org.junit.Assert.*;

import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:camel-context-test.xml" })
public class ITProxyRoute {

	private static Properties testProperties;
	private static ConnectionFactory factory;

	private HttpTestServer server;

	@Autowired
	CamelContext camelCtx;

	@BeforeClass
	public static void setUp() throws IOException {
		// Load properties
		testProperties = new Properties();
		InputStream is = ITProxyRoute.class
				.getResourceAsStream("/proxy-test.properties");
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
		// Prepare mocked Server
		server = new HttpTestServer(8899);
	}
	
	@Test
	public void testPatientSearchWithNull() throws Exception {
		
		// Build Oink request
		String resourcePath = "/Patient";
		String method = "GET";
		OINKRequestMessage request = new OINKRequestMessage();
		request.setResourcePath(resourcePath);
		request.setMethod(HttpMethod.valueOf(method));

		// Send Oink request over rabbit
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		String replyQueueName = channel.queueDeclare().getQueue();
		channel.queueBind(replyQueueName, testProperties.getProperty("rabbit.defaultExchange"), replyQueueName);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);

		BasicProperties props = new BasicProperties().builder()
				.replyTo(replyQueueName).build();

		byte[] requestBody = camelCtx.getTypeConverter().convertTo(
				byte[].class, request);

		channel.basicPublish(
				testProperties.getProperty("rabbit.defaultExchange"),
				testProperties.getProperty("rabbit.routingKey"), props,
				requestBody);
		
		// Wait
		Thread.sleep(1000);
		
		// Assert response
		QueueingConsumer.Delivery delivery = consumer.nextDelivery(5000);
		assertNotNull(delivery);
		byte[] responseBody = delivery.getBody();
		
		OinkMessageConverter conv = new OinkMessageConverter();
		OINKResponseMessage message = conv.responseMessageFromByteArray(responseBody);
		
	}
	
	@Ignore
	@Test
	public void testGetRequestGetsProxied() throws Exception {
		
		// Mock endpoint server
		InputStream is = ITProxyRoute.class.getResourceAsStream("/patient.json");
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String jsonPatient = writer.toString();
		byte[] expectedResponseBody = jsonPatient.getBytes();
		server.setResponse(200, expectedResponseBody, "application/json+fhir");
		server.start();

		// Build Oink request
		String resourcePath = "/Patient/1123";
		String parameters = "foo=bar&foo2=bar2";
		String method = "GET";
		OINKRequestMessage request = new OINKRequestMessage();
		request.setResourcePath(resourcePath);
		request.setParameters(parameters);
		request.setMethod(HttpMethod.valueOf(method));

		// Send Oink request over rabbit
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		String replyQueueName = channel.queueDeclare().getQueue();
		channel.queueBind(replyQueueName, testProperties.getProperty("rabbit.defaultExchange"), replyQueueName);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);

		BasicProperties props = new BasicProperties().builder()
				.replyTo(replyQueueName).build();

		byte[] requestBody = camelCtx.getTypeConverter().convertTo(
				byte[].class, request);

		channel.basicPublish(
				testProperties.getProperty("rabbit.defaultExchange"),
				testProperties.getProperty("rabbit.routingKey"), props,
				requestBody);
		
		// Wait
		Thread.sleep(1000);
		
		// Assert mocked server receives request intact
		assertEquals(method, server.getRequestMethod());
		assertEquals(parameters, server.getRequestParams());
		assertEquals("/oink"+resourcePath, server.getRequestPath());
		
		// Assert response
		QueueingConsumer.Delivery delivery = consumer.nextDelivery(5000);
		assertNotNull(delivery);
		byte[] responseBody = delivery.getBody();

		OINKResponseMessage response = camelCtx.getTypeConverter().convertTo(
				OINKResponseMessage.class, responseBody);
		

		String responsePatientJson = camelCtx.getTypeConverter().convertTo(String.class, response.getBody().getResource());
		
		assertEquals(200, response.getStatus());
		assertEquals(jsonPatient, responsePatientJson);
		server.stop();	
		
	}

	@Ignore
	@Test
	public void testPostRequestGetsProxied() throws Exception {

		// Mock endpoint server
		server.setResponse(201, null, null);
		server.start();

		// Build Oink request
		OINKRequestMessage request = new OINKRequestMessage();
		request.setResourcePath("/Patient");
		request.setMethod(HttpMethod.POST);
		FhirBody body = buildFhirBodyFromResource("/patient.json");
		request.setBody(body);

		// Send Oink request over rabbit
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		String replyQueueName = channel.queueDeclare().getQueue();
		channel.queueBind(replyQueueName, testProperties.getProperty("rabbit.defaultExchange"), replyQueueName);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(replyQueueName, true, consumer);

		BasicProperties props = new BasicProperties().builder()
				.replyTo(replyQueueName).build();

		byte[] requestBody = camelCtx.getTypeConverter().convertTo(
				byte[].class, request);

		channel.basicPublish(
				testProperties.getProperty("rabbit.defaultExchange"),
				testProperties.getProperty("rabbit.routingKey"), props,
				requestBody);

		// Wait
		Thread.sleep(1000);
		
		// Assert mocked server receives request intact
		String requestBodyReceivedByServer = server.getRequestBody();
		StringWriter writer = new StringWriter();
		IOUtils.copy(ITProxyRoute.class.getResourceAsStream("/patient.json"), writer);
		String expectedBodyReceivedByServer = writer.toString();
		assertEquals(expectedBodyReceivedByServer, requestBodyReceivedByServer);

		// Assert response
		QueueingConsumer.Delivery delivery = consumer.nextDelivery(10000);
		assertNotNull(delivery);
		byte[] responseBody = delivery.getBody();

		OINKResponseMessage response = camelCtx.getTypeConverter().convertTo(
				OINKResponseMessage.class, responseBody);
		

		assertEquals(201, response.getStatus());
		server.stop();
	}

	
	private static FhirBody buildFhirBodyFromResource(String resourcePath) throws Exception {
		InputStream is = ITProxyRoute.class.getResourceAsStream(resourcePath);
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
	
}
