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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;

import uk.org.openeyes.oink.exception.OinkException;

public class XmlTransformer {
	
	TransformerFactory factory;
	
	public XmlTransformer() {
		factory = new net.sf.saxon.TransformerFactoryImpl();
	}

	public String transform(String inputXml, InputStream xslIs) throws TransformerFactoryConfigurationError, TransformerException, UnsupportedEncodingException, OinkException {

		Source xslSource = new StreamSource(xslIs);

		InputStream inputXmlIs = new ByteArrayInputStream(inputXml.getBytes());
		Source inputXmlSource = new StreamSource(inputXmlIs);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		Result outputXmlResult = new StreamResult(os);

		Transformer transformer = factory.newTransformer(xslSource);
		
		if (transformer == null) {
			throw new OinkException("Failed to transform XML using XSL");
		}
		
		transformer.transform(inputXmlSource, outputXmlResult);
		return os.toString("UTF-8");
	}

}
