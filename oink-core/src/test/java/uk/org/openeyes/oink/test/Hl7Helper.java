package uk.org.openeyes.oink.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class Hl7Helper {
	
	public static Message loadHl7Message(String path) throws IOException, HL7Exception {
		InputStream is = Hl7Helper.class.getResourceAsStream(path);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String message = writer.toString();
		HapiContext context = new DefaultHapiContext();

		context.setValidationContext(new NoValidation());
		Parser p = context.getGenericParser();
		Message adt = p.parse(message);
		return adt;
	}

}
