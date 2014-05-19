package uk.org.openeyes.oink.hl7v2.test;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class ITHl7v2ToRabbitRoute extends Hl7ITSupport {
	
	@Before
	public void setUp() throws IOException {
		setProperties("/hl7v2-test.properties");
	}
	
	@Test
	public void testIncomingA28IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A28-1.txt", "/oinkrequestmessages/A28-1.json");
	}
	
	@Test
	public void testIncomingA01IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A01.txt", "/oinkrequestmessages/A01.json");
	}

	@Test
	public void testIncomingA05IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A05.txt", "/oinkrequestmessages/A05.json");
	}

	@Test
	public void testIncomingA31IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A31-2.txt", "/oinkrequestmessages/A31-2.json");
	}
	
	@Test
	public void testIncomingA40IsProcessedAndRouted() throws HL7Exception, IOException, LLPException, InterruptedException, JSONException {
		testIncomingMessageIsProcessedAndRouted("/hl7v2/A40-1.txt", "/oinkrequestmessages/A40-1.json");
	}

}
