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
import java.util.Properties;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.rabbitmq.RabbitMQEndpoint;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.domain.json.OinkResponseMessageJsonConverter;
import uk.org.openeyes.oink.hl7v2.ADTProcessor;
import uk.org.openeyes.oink.rabbit.SynchronousRabbitTimeoutException;
import uk.org.openeyes.oink.test.RabbitServer;
import uk.org.openeyes.oink.test.RabbitTestUtils;
import ca.uhn.hl7v2.model.Message;

import com.rabbitmq.client.QueueingConsumer.Delivery;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class TestHl7v2Messages extends Hl7TestSupport {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TestHl7v2Messages.class);
	
	@EndpointInject(uri="rabbitmq:5672/oink")
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
	
	@Qualifier("a01Processor")
	@Autowired
	ADTProcessor a01Processor;

	@Test
	public void testIdentifierRemap() throws Exception {
		Patient patient = (Patient)processResource(a01Processor, "/example-messages/hl7v2/A01.txt");
		
		Assert.assertEquals("Bloggs", patient.getName().get(0).getFamily().get(0).getValue());
	}
	
	protected OINKResponseMessage process(ADTProcessor processor, String testMessageResourceFile) throws Exception {
		
		// Choose a message to send
		Message m = Hl7TestUtils.loadHl7Message(testMessageResourceFile);
		
		// Prepare dead letter queue
		RabbitServer server = new RabbitServer(getProperty("rabbit.host"),
				Integer.parseInt(getProperty("rabbit.port")),
				getProperty("rabbit.vhost"), getProperty("rabbit.username"),
				getProperty("rabbit.password"));
		server.setConsumingDetails(getProperty("rabbit.defaultExchange"), getProperty("rabbit.outboundRoutingKey"));
		server.start();
		
		processor.setResolveCareProvider(false);
		processor.setResolveManagingOrganization(false);
		try {
			processor.process(m, endpoint.createExchange());
		} catch(SynchronousRabbitTimeoutException e) {
			// ignore this exception as not connected to a remote host
		} catch(CamelExecutionException e) {
			// ignore this exception as not connected to a remote host
		}
		
		// Assert message is received
		Delivery delivery = server.getDelivery();
		Assert.assertNotNull(delivery.getBody());
		
		server.stop();
		
		OinkResponseMessageJsonConverter converter = new OinkResponseMessageJsonConverter();
		OINKResponseMessage response = converter.fromJsonString(new String(delivery.getBody()));
		
		return response;
	}
	
	protected Resource processResource(ADTProcessor processor, String testMessageResourceFile) throws Exception {
		OINKResponseMessage r = process(processor, testMessageResourceFile);
		return r.getBody().getResource();
	}
}
