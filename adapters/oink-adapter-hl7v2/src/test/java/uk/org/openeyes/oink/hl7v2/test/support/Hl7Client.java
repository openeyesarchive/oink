package uk.org.openeyes.oink.hl7v2.test.support;

import java.io.IOException;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;

public class Hl7Client {
	
	public static Message send(Message message, String host, int port) throws HL7Exception, LLPException, IOException {
		HapiContext context = new DefaultHapiContext();
		ca.uhn.hl7v2.app.Connection hl7v2Conn = context.newClient(host, port,
				false);
		Initiator initiator = hl7v2Conn.getInitiator();
		Message response = initiator.sendAndReceive(message);
		hl7v2Conn.close();
		return response;
	}
	
}