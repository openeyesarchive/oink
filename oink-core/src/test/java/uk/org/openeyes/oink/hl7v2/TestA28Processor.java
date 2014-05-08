package uk.org.openeyes.oink.hl7v2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class TestA28Processor {
	
	private A28Processor processor;
	
	@Before
	public void before() throws IOException {
		processor = new A28Processor();
		processor.setXsltPath("/uk/org/openeyes/oink/hl7v2/a28.xsl");
	}
	
	@Test
	public void testProcessorHandlesValidA28Message() throws Exception {
		Message a28message = buildMessageFromResource("/hl7v2/A28-1.txt");
		OINKRequestMessage message = processor.process(a28message);
		assertEquals(HttpMethod.POST, message.getMethod());
		assertEquals("/Patient", message.getResourcePath());
		fail("Have not implemented FHIR Body assertion");
		
	}
	
	
	public static Message buildMessageFromResource(String pathToFile) throws IOException, HL7Exception {
		String message = loadResourceAsString(pathToFile);
		HapiContext hapi = new DefaultHapiContext();
		hapi.setValidationContext(new NoValidation());
		Parser p = hapi.getGenericParser();
		Message m = p.parse(message);
		return m;
	}
	
	public static String loadResourceAsString(String resourcePath) throws IOException {
		InputStream is = TestA28Processor.class.getResourceAsStream(resourcePath);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
	}

}
