package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ADT_A01;

public class TestMessageXMLConverter extends Hl7TestSupport {

	@Test
	public void testCanDecodeSampleA01MessageFromXml() throws IOException, HL7Exception {
		String xml = loadResourceAsString("/hl7v2/A01.XML");
		MessageConverter converter = new MessageConverter();
		Message m = converter.fromXml(xml);
		Assert.assertTrue(m instanceof ADT_A01);
	}
	
	@Test
	@Ignore
	public void convertPipeToXml() throws HL7Exception, IOException {
		String message = "ADR-A19";
		Message msg = loadMessage("/hl7v2/"+message+".txt");
		MessageConverter conv = new MessageConverter();
		String xml = conv.toXml(msg);
		PrintWriter out = new PrintWriter("/Users/Oli/"+message+".xml");
		out.print(xml);
		out.close();
	}
	
	@Test
	public void testEncodingAndDecodingTheSameXMLMessageProducesTheSameOutput() throws IOException, HL7Exception, SAXException {
		String expectedXml = loadResourceAsString("/hl7v2/A01.XML");
		MessageConverter converter = new MessageConverter();
		Message m = converter.fromXml(expectedXml);
		Assert.assertTrue(m instanceof ADT_A01);
		String gotXml = converter.toXml(m);
		XMLUnit.setIgnoreWhitespace(true);
		XMLAssert.assertXMLEqual(expectedXml, gotXml);		
	}
	
	public String loadResourceAsString(String resourcePath) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(resourcePath);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		return writer.toString();
	}
	
}
