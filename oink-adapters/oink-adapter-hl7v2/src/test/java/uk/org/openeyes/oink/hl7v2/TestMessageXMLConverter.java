/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.lang.StringUtils;
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
		String xml = loadResourceAsString("/example-messages/hl7v2/A01.xml");
		Hl7v2XmlConverter converter = new Hl7v2XmlConverter();
		Message m = converter.fromXml(xml);
		Assert.assertTrue(m instanceof ADT_A01);
	}
	
	@Test
	public void testCanLoadExampleA19Result() throws Exception {
		Message msg = loadMessage("/example-messages/hl7v2/ADR-A19-mod.txt");
		Hl7v2XmlConverter conv = new Hl7v2XmlConverter();
		String xml = conv.toXml(msg);
		Assert.assertFalse(StringUtils.isEmpty(xml));
	}
	
	@Test
	@Ignore
	public void convertPipeToXml() throws HL7Exception, IOException {
		String message = "ADR-A19";
		Message msg = loadMessage("/example-messages/hl7v2/"+message+".txt");
		Hl7v2XmlConverter conv = new Hl7v2XmlConverter();
		String xml = conv.toXml(msg);
		PrintWriter out = new PrintWriter("/Users/Oli/"+message+".xml");
		out.print(xml);
		out.close();
	}
	
	@Test
	public void testEncodingAndDecodingTheSameXMLMessageProducesTheSameOutput() throws IOException, HL7Exception, SAXException {
		String expectedXml = loadResourceAsString("/example-messages/hl7v2/A01.xml");
		Hl7v2XmlConverter converter = new Hl7v2XmlConverter();
		Message m = converter.fromXml(expectedXml);
		Assert.assertTrue(m instanceof ADT_A01);
		String gotXml = converter.toXml(m);
		XMLUnit.setIgnoreWhitespace(true);
		XMLAssert.assertXMLEqual(expectedXml, gotXml);		
	}
	
}
