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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ACK;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.hl7v2.ADTProcessor;
import uk.org.openeyes.oink.hl7v2.Hl7v2Processor;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;
import uk.org.openeyes.oink.test.Hl7TestUtils;
import uk.org.openeyes.oink.test.RabbitTestUtils;

/**
 * 
 * Tests the main HLv2 routing whilst mocking the {@link Hl7v2Processor}
 * processors which are responsible for converting the received (unvalidated)
 * {@link Message} into the appropriate {@link OINKRequestMessage}.
 * 
 * This test has no dependencies on the OpenMapsSW part.
 * 
 * The test methods here are to ensure that the Route is correctly configured.
 * 
 * @author Oliver Wilkie
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class TestHl7v2ToRabbitRouteWithoutProcessors extends Hl7TestSupport {

	@Qualifier("a01Processor")
	@Autowired
	ADTProcessor a01Processor;

	@Qualifier("a05Processor")
	@Autowired
	ADTProcessor a05Processor;

	@Qualifier("a28Processor")
	@Autowired
	ADTProcessor a28Processor;

	@Qualifier("a31Processor")
	@Autowired
	ADTProcessor a31Processor;

	ConnectionFactory rabbitFactory;
	
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
		rabbitFactory = initRabbit(getProperty("rabbit.host"),
				Integer.parseInt(getProperty("rabbit.port")),
				getProperty("rabbit.username"), getProperty("rabbit.password"),
				getProperty("rabbit.vhost"));
	}

	@Test
	@DirtiesContext
	public void testA01MessageRoutesOntoRabbit() throws Exception {

		// Choose a message to send
		Message m = Hl7TestUtils.loadHl7Message("/example-messages/hl7v2/A01.txt");

		// Prepare mocks
		String oinkJson = loadResourceAsString("/example-messages/oinkrequestmessages/A01.json");
		OinkMessageConverter conv = new OinkMessageConverter();
		OINKRequestMessage mockRequest = conv.fromJsonString(oinkJson);


		testRouteFilter(m,mockRequest);

		// Check mocks
		verify(a01Processor).process(any(Message.class), any(Exchange.class));
		verify(a05Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a28Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a31Processor, never()).process(any(Message.class),
				any(Exchange.class));

	}

	@Test
	@DirtiesContext
	public void testA05MessageRoutesOntoRabbit() throws Exception {

		// Choose a message to send
		Message m = Hl7TestUtils.loadHl7Message("/example-messages/hl7v2/A05.txt");

		// Prepare mocks
		String oinkJson = loadResourceAsString("/example-messages/oinkrequestmessages/A05.json");
		OinkMessageConverter conv = new OinkMessageConverter();
		OINKRequestMessage mockRequest = conv.fromJsonString(oinkJson);

		testRouteFilter(m, mockRequest);

		// Check mocks
		verify(a01Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a05Processor).process(any(Message.class), any(Exchange.class));
		verify(a28Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a31Processor, never()).process(any(Message.class),
				any(Exchange.class));
	}

	@Test
	@DirtiesContext
	public void testA28MessageRoutesOntoRabbit() throws Exception {

		// Choose a message to send
		Message m = Hl7TestUtils.loadHl7Message("/example-messages/hl7v2/A28-1.txt");

		// Prepare mocks
		String oinkJson = loadResourceAsString("/example-messages/oinkrequestmessages/A28-1.json");
		OinkMessageConverter conv = new OinkMessageConverter();
		OINKRequestMessage mockRequest = conv.fromJsonString(oinkJson);

		testRouteFilter(m, mockRequest);

		// Check mocks
		verify(a01Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a28Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a05Processor).process(any(Message.class), any(Exchange.class));
		verify(a31Processor, never()).process(any(Message.class),
				any(Exchange.class));

	}

	@Test
	@DirtiesContext
	public void testA31MessageRoutesOntoRabbit() throws Exception {

		// Choose a message to send
		Message m = Hl7TestUtils.loadHl7Message("/example-messages/hl7v2/A31-2.txt");

		// Prepare mocks
		String oinkJson = loadResourceAsString("/example-messages/oinkrequestmessages/A31-2.json");
		OinkMessageConverter conv = new OinkMessageConverter();
		OINKRequestMessage mockRequest = conv.fromJsonString(oinkJson);

		testRouteFilter(m, mockRequest);

		// Check mocks
		verify(a01Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a31Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a28Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a05Processor).process(any(Message.class), any(Exchange.class));
	}

	private void testRouteFilter(Message m,
			OINKRequestMessage r) throws Exception {

		// Send message
		String host = getProperty("hl7v2.host");
		int port = Integer.parseInt(getProperty("hl7v2.port"));
		Message responseMessage = sendHl7Message(m, host, port);
		ACK acknowledgement = (ACK) responseMessage;
		assertEquals("AA", acknowledgement.getMSA().getAcknowledgementCode()
				.getValue());

	}

	@Test
	public void testA40MessageDoesNotRouteOntoRabbit() throws Exception {
		// Init Rabbit listener
		Channel c = getChannel(rabbitFactory);
		String queueName = setupRabbitQueue(c,
				getProperty("rabbit.defaultExchange"),
				getProperty("rabbit.outboundRoutingKey"));

		// Choose a message to send
		Message m = Hl7TestUtils.loadHl7Message("/example-messages/hl7v2/A40-1.txt");

		// Send message
		String host = getProperty("hl7v2.host");
		int port = Integer.parseInt(getProperty("hl7v2.port"));
		Message responseMessage = sendHl7Message(m, host, port);
		ACK acknowledgement = (ACK) responseMessage;
		assertEquals("AR", acknowledgement.getMSA().getAcknowledgementCode()
				.getValue());

		// Consume message from rabbit
		byte[] body = receiveRabbitMessage(c, queueName, 1000);
		assertNull(body);

		// Close rabbit connection
		c.close();

		// Check mocks
		verify(a01Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a05Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a28Processor, never()).process(any(Message.class),
				any(Exchange.class));
		verify(a31Processor, never()).process(any(Message.class),
				any(Exchange.class));
	}

}
