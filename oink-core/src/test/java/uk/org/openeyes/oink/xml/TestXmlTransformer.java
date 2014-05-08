package uk.org.openeyes.oink.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestXmlTransformer {
	
	
	@Test
	public void testSimpleTransform() throws IOException, TransformerFactoryConfigurationError, TransformerException {

		String inputXml = getResourceAsString("/hl7v2/A01-.xml");
		String xsl = getResourceAsString("/uk/org/openeyes/oink/hl7v2/a01.xsl");
		
		XmlTransformer transformer = new XmlTransformer();
		String result = transformer.transform(inputXml,xsl);
		fail("Not yet implemented");
	}
	
	private static String getResourceAsString(String s) throws IOException {
		InputStream is = TestXmlTransformer.class.getResourceAsStream(s);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
	}

}
