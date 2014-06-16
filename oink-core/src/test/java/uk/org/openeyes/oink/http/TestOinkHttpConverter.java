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
package uk.org.openeyes.oink.http;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.junit.Test;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

public class TestOinkHttpConverter {

	@Test
	public void testReadFhirBody() throws Exception {

		OinkMessageHttpProcessor conv = new OinkMessageHttpProcessor();

		InputStream is = TestOinkHttpConverter.class
				.getResourceAsStream("/example-messages/fhir/searchResults.json");

		FhirBody body = conv.readFhirBody(is);

	}

	@Test
	public void testCanHandleEmptySearchResults() throws Exception {
		OinkMessageHttpProcessor conv = new OinkMessageHttpProcessor();

		InputStream inputStream = this.getClass().getResourceAsStream(
				"/example-messages/fhir/noSearchResults.json");
		assertNotNull(inputStream);

		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		String theString = writer.toString();

		OINKResponseMessage resp = conv.buildOinkResponse(
				new HashMap<String, Object>(), theString);
		assertNotNull(resp.getBody());
		assertNotNull(resp.getBody().getBundle());

	}

}
