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
package uk.org.openeyes.oink.fhir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.Converter;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.AtomFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import uk.org.openeyes.oink.exception.OinkException;

/**
 * 
 * Parses Bundle/AtomFeeds from JSON or XML input streams.
 * 
 * @author Oliver Wilkie
 */
@Converter
public class BundleParser {

	private final JsonParser jsonParser;
	private final XmlParser xmlParser;

	private static final Logger log = LoggerFactory
			.getLogger(BundleParser.class);

	public BundleParser() {
		xmlParser = new XmlParser();
		jsonParser = new JsonParser();
	}

	@Converter
	public AtomFeed fromJsonOrXml(String input) throws FhirConversionException {
		if (input.contains("\"\"")) {
			log.warn("Possible empty string value detected in input. Valid FHIR doesn't contain empty strings!");
		}
		AtomFeed f = null;
		try {
			InputStream is = new ByteArrayInputStream(input.getBytes());
			f = xmlParser.parseGeneral(is).getFeed();
		} catch (Exception e) {
			InputStream is = new ByteArrayInputStream(input.getBytes());
			try {
				ResourceOrFeed resOrFeed = jsonParser.parseGeneral(is);
				f = resOrFeed.getFeed();
			} catch (Exception e1) {
				log.error(e1.toString());
				throw new FhirConversionException();
			}
		}
		return f;
	}

	public AtomFeed fromXmlToBundle(String xml) throws Exception {
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			AtomFeed f = xmlParser.parseGeneral(is).getFeed();
			return f;
		} catch (XmlPullParserException parserEx) {
			log.error("Failed to parse XML content. Structure may not be valid");
			throw new OinkException("Failed to parse XML content. Message is:"
					+ parserEx.getMessage());
		}
	}

}
