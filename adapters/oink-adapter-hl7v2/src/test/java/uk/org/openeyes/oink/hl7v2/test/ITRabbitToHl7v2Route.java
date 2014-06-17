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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.camel.CamelContext;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Patient;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.datatype.XCN;
import ca.uhn.hl7v2.model.v24.message.QRY_A19;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;
import uk.org.openeyes.oink.proxy.test.support.RabbitClient;
import uk.org.openeyes.oink.test.Hl7Helper;
import uk.org.openeyes.oink.test.Hl7Server;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class ITRabbitToHl7v2Route extends Hl7ITSupport {

	@Autowired
	CamelContext ctx;
	
	@BeforeClass
	public static void before() throws IOException {
		Properties props = new Properties();
		InputStream is = TestHl7v2ToRabbitRoute.class.getResourceAsStream("/hl7v2-test.properties");
		props.load(is);
		Assume.assumeTrue("No RabbitMQ Connection detected", Hl7ITSupport.isRabbitMQAvailable(props));
	}
	
	@Before
	public void setUp() throws IOException {
		setProperties("/hl7v2-test.properties");
	}

	@Test
	public void testAnIncomingPatientSearchByHSIDNumberIsMapped()
			throws Exception {
		
		final String TEST_HISID_NUMBER = "7111111";

		// Create Sample OINK Request Message
		OINKRequestMessage requestMessage = new OINKRequestMessage();
		requestMessage.setResourcePath("/Patient");
		requestMessage.setMethod(HttpMethod.GET);
		requestMessage.setParameters("identifier=HISID|"+TEST_HISID_NUMBER);

		// Init HL7v2
		Hl7Server server = new Hl7Server(
				Integer.parseInt(getProperty("remote.port")), false);
		ReceivingApplication application = new ReceivingApplication() {

			@Override
			public Message processMessage(Message theMessage,
					Map<String, Object> theMetadata)
					throws ReceivingApplicationException, HL7Exception {
				try {
					Message m = Hl7Helper.loadHl7Message("/example-messages/oinkrequestmessages/ADR-A19.json");
					return m;
				} catch (IOException e) {
					throw new HL7Exception(e);
				}
			}

			@Override
			public boolean canProcess(Message theMessage) {
				return true;
			}
		};
		server.setMessageHandler("QRY", "A19", application);
		server.start();

		// Send Sample over rabbit
		RabbitClient client = new RabbitClient(getProperty("rabbit.host"),
				Integer.parseInt(getProperty("rabbit.port")),
				getProperty("rabbit.vhost"), getProperty("rabbit.username"),
				getProperty("rabbit.password"));
		
		byte[] payload = ctx.getTypeConverter().convertTo(byte[].class, requestMessage);
		
		// Send and receive
		byte[] responsePayload = client.sendAndRecieve(payload, getProperty("rabbit.inboundRoutingKey"), getProperty("rabbit.defaultExchange"));
		
		// Check what the Hl7Server received
		Message receivedHl7Message = server.getReceivedMessage();
		assertNotNull(receivedHl7Message);
		QRY_A19 a19 = (QRY_A19) receivedHl7Message;
		assertTrue(receivedHl7Message instanceof QRY_A19);
		// Check Hl7Server recieved NHS Number request
		XCN xcn = a19.getQRD().getQrd8_WhoSubjectFilter(0);
		assertEquals(TEST_HISID_NUMBER, xcn.getIDNumber().getValue());
		assertEquals("HISID", xcn.getIdentifierTypeCode().getValue());
		
		
		// Check the OinkResponseMessage recieved
		
		assertNotNull(responsePayload);
		OinkMessageConverter conv = new OinkMessageConverter();
		OINKResponseMessage response = conv.responseMessageFromByteArray(responsePayload);
		assertNotNull(response);
		assertEquals(200, response.getStatus());
		
		// Check contents of the OinkResponseMessage
		String expectedJson = loadResourceAsString("/example-messages/oinkrequestmessages/ADR-A19.json");
		assertEquals(expectedJson, conv.toJsonString(response));
		
	}

}
