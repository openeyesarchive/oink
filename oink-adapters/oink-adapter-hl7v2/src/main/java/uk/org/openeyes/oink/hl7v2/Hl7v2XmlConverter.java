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
