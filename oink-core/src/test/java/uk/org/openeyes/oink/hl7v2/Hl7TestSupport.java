package uk.org.openeyes.oink.hl7v2;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class Hl7TestSupport {
	
	protected static Message loadMessage(String path) throws IOException, HL7Exception {
		InputStream is = Hl7TestSupport.class.getResourceAsStream(path);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String message = writer.toString();
		HapiContext context = new DefaultHapiContext();

		context.setValidationContext(new NoValidation());
		Parser p = context.getGenericParser();
		Message adt = p.parse(message);
		return adt;
	}
	
	public static void testProcessorProducesExpectedOutput(Hl7v2Processor processor, String examplePath, String expectedPath) throws Exception {
		Message hl7Message = loadMessage(examplePath);
		OINKRequestMessage message = processor.process(hl7Message);		
		OinkMessageConverter conv = new OinkMessageConverter();
		String generatedJson = conv.toJsonString(message);
		String expectedJson = loadResourceAsString(expectedPath);
		assertEquals(expectedJson, generatedJson);
	}
	
	public static String loadResourceAsString(String resourcePath) throws IOException {
		InputStream is = Hl7TestSupport.class.getResourceAsStream(resourcePath);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
	}
	
	protected class NestedResourceIdGenerator {
		
		private int count = 1;
		
		private String prefix = "id";
		
		public String getNext() {
			String s = prefix + count;
			count++;
			return s;
		}
		
	}

}
