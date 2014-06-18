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
package uk.org.openeyes.oink.hl7v2.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.rabbitmq.RabbitMQEndpoint;
import org.apache.camel.test.spring.MockEndpoints;
import org.apache.camel.test.spring.MockEndpointsAndSkip;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.test.Hl7TestUtils;
import uk.org.openeyes.oink.test.RabbitServer;
import uk.org.openeyes.oink.test.RabbitTestUtils;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class TestHl7v2ToRabbitRoute extends Hl7TestSupport {
	
	@Autowired
	private CamelContext ctx;
	
	@EndpointInject(uri="rabbitmq:5672/test")
	private RabbitMQEndpoint endpoint;
	
	@BeforeClass
	public static void before() throws IOException {
		Properties props = new Properties();
		InputStream is = TestHl7v2ToRabbitRoute.class.getResourceAsStream("/hl7v2-test.properties");
		props.load(is);
		Assume.assumeTrue("No RabbitMQ Connection detected", RabbitTestUtils.isRabbitMQAvailable(props));
	}
	
	@Before
	public void setUp() throws IOException {
		setProperties("/hl7v2-test.properties");
	}
	
	@Test
	public void testARejectableMessageIsPlacedOnDeadLetterQueue() throws HL7Exception, IOException, LLPException, InterruptedException {
		// Load a message that will be rejected
		Message message = Hl7TestUtils.loadHl7Message("/example-messages/hl7v2/A40-1.txt");
		
		// Prepare dead letter queue
		RabbitServer server = new RabbitServer(getProperty("rabbit.host"),
				Integer.parseInt(getProperty("rabbit.port")),
				getProperty("rabbit.vhost"), getProperty("rabbit.username"),
				getProperty("rabbit.password"));
		server.setConsumingDetails(getProperty("rabbit.defaultExchange"), getProperty("rabbit.deadLetterRoutingKey"));
		server.start();
		
		// Send message to adapter
		String host = getProperty("hl7v2.host");
		Integer port = Integer.parseInt(getProperty("hl7v2.port"));
		Hl7TestUtils.sendTCP(message, host, port);
		
		// Assert deadletter endpoint is called
		byte[] deadLetterMessage = server.getReceivedMessage();
		Assert.assertNotNull(deadLetterMessage);
		server.stop();
		
	}
	
	@Ignore
	@Test
	public void testIncomingA28IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/example-messages/hl7v2/A28-1.txt", "/example-messages/oinkrequestmessages/A28-1.json");
	}
	
	@Ignore
	@Test
	public void testIncomingA01IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A01.txt", "/oinkrequestmessages/A01.json");
	}

	@Ignore
	@Test
	public void testIncomingA05IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A05.txt", "/oinkrequestmessages/A05.json");
	}

	@Ignore
	@Test
	public void testIncomingA31IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A31-2.txt", "/oinkrequestmessages/A31-2.json");
	}
	

}
