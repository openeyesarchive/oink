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
package uk.org.openeyes.oink.xml;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.StringUtils;

import uk.org.openeyes.oink.exception.OinkException;

public class TestXmlTransformer {
	
	
	@Ignore
	@Test
	public void testSimpleTransform() throws IOException, TransformerFactoryConfigurationError, TransformerException, OinkException {

		String inputXml = getResourceAsString("/example-messages/hl7v2/A28-3.xml");
		InputStream xsl = TestXmlTransformer.class.getResourceAsStream("/uk/org/openeyes/oink/hl7v2/A28.xsl");
		
		XmlTransformer transformer = new XmlTransformer();
		String result = transformer.transform(inputXml,xsl);
		Assert.assertTrue(StringUtils.hasText(result));
		fail("Not yet implemented");
	}
	
	private static String getResourceAsString(String s) throws IOException {
		InputStream is = TestXmlTransformer.class.getResourceAsStream(s);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer, "UTF-8");
		return writer.toString();
	}

}
