package uk.org.openeyes.oink.hl7v2.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class TestHl7v2ToRabbitRoute extends Hl7TestSupport {
	
	@Autowired
	CamelContext ctx;
	
	@Before
	public void setUp() throws IOException {
		setProperties("/hl7v2-test.properties");
	}
	
	@Test
	public void testIncomingA28IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException {
		
		// Choose a message to send
		Message m = loadMessage("/samples/A28-1.txt");
		
		// Prepare RabbitServer
		RabbitServer server = new RabbitServer(getProperty("rabbit.host"),
				Integer.parseInt(getProperty("rabbit.port")),
				getProperty("rabbit.vhost"), getProperty("rabbit.username"),
				getProperty("rabbit.password"));
		server.setConsumingDetails(getProperty("rabbit.defaultExchange"), getProperty("rabbit.outboundRoutingKey"));
		server.start();
		
		// Send HL7v2 message
		String host = getProperty("hl7v2.host");
		int port = Integer.parseInt(getProperty("hl7v2.port"));
		Message responseMessage = HL7Client.send(m, host, port);
		
		Thread.sleep(1000);
		
		// Check received Rabbit message
		byte[] receivedMessage = server.getReceivedMessage();
		server.stop();
		
		assertNotNull(receivedMessage);
		
		OINKRequestMessage request = ctx.getTypeConverter().convertTo(OINKRequestMessage.class, receivedMessage);
		
		assertEquals("/Patient",request.getResourcePath());
		assertEquals(HttpMethod.POST, request.getMethod());
		
		fail("Final assertion not yet implmented");
	}

}
