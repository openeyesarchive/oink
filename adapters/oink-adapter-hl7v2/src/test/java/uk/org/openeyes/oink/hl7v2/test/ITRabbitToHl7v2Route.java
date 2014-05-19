package uk.org.openeyes.oink.hl7v2.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Patient;
import org.junit.Before;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class ITRabbitToHl7v2Route extends Hl7ITSupport {

	@Autowired
	CamelContext ctx;
	
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
		HL7Server server = new HL7Server(
				Integer.parseInt(getProperty("remote.port")), false);
		ReceivingApplication application = new ReceivingApplication() {

			@Override
			public Message processMessage(Message theMessage,
					Map<String, Object> theMetadata)
					throws ReceivingApplicationException, HL7Exception {
				try {
					Message m = loadHl7Message("/oinkrequestmessages/ADR-A19.json");
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
		String expectedJson = loadResourceAsString("/oinkrequestmessages/ADR-A19.json");
		assertEquals(expectedJson, conv.toJsonString(response));
		
	}

}
