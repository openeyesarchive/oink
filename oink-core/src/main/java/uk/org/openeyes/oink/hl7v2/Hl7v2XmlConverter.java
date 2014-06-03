package uk.org.openeyes.oink.hl7v2;

import org.apache.camel.Body;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.XMLParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * 
 * Serialises HL7v2 Java objects to XML format and deserialises XML format to
 * equivalent HL7v2 Java Objects
 * 
 * @author Oliver Wilkie
 */
public class Hl7v2XmlConverter {

	private HapiContext context;
	private XMLParser parser;

	public Hl7v2XmlConverter() {
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
