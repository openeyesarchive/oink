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
package uk.org.openeyes.oink.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.camel.Converter;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;

@Converter
public class FhirResourceConverter {

	@Converter
	public static String toJsonString(Resource body) throws Exception {
		JsonComposer composer = new JsonComposer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		composer.compose(os, body, false);
		return os.toString("UTF-8");
	}
	
	@Converter
	public static Resource fromJsonString(String string) throws Exception {
		JsonParser parser = new JsonParser();
		ByteArrayInputStream is = new ByteArrayInputStream(string.getBytes());
		return parser.parse(is);
	}
	
}
