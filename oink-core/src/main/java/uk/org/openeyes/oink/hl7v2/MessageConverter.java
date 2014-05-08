package uk.org.openeyes.oink.hl7v2;

import org.apache.camel.Body;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class MessageConverter {

	private HapiContext context;
	private XMLParser parser;
	
	public MessageConverter() {
		context = new DefaultHapiContext();
		context.setValidationContext(new NoValidation());
		parser = context.getXMLParser();
	}
	
	public String toXml(@Body Message message) throws HL7Exception {
		String messageInXml = parser.encode(message);
		return messageInXml;
	}
	
	public Message fromXml(String xml) throws HL7Exception {
		Message message = parser.parse(xml);
		return message;
	}

}
