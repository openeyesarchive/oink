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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private boolean fixZTags = true;

	public Hl7v2XmlConverter() {
		context = new DefaultHapiContext();
		context.setValidationContext(new NoValidation());
		parser = context.getXMLParser();
	}

	public String toXml(@Body Message message) throws HL7Exception {
		
		if(fixZTags) {
			// Fix invalid Z tags so that they are of the format ZXX
			
			String messageString = message.encode();

			Pattern pattern = Pattern.compile("Z\\S+\\|");
			Matcher matcher = pattern.matcher(messageString);

			StringBuffer sb = new StringBuffer();
			int z = 0;
			while (matcher.find()) {
				z++;
				matcher.appendReplacement(sb,
						"Z" + String.format("%02d", z) + "|");
			}
			matcher.appendTail(sb);
			messageString = sb.toString();

			messageString = messageString.replace("\n", "\r");
			
			message.parse(messageString);
		}
		
		String messageInXml = parser.encode(message);
		return messageInXml;
	}

	public Message fromXml(String xml) throws HL7Exception {
		Message message = parser.parse(xml);
		return message;
	}
	
	public boolean isFixZTags() {
		return fixZTags;
	}

	public void setFixZTags(boolean fixZTags) {
		this.fixZTags = fixZTags;
	}
}
