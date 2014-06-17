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

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.AtomFeed;
import org.junit.Assert;
import org.junit.Test;

public class TestFhirConverter {
	
	@Test
	public void testCanParsePractionerSearchResultsFromOpenEyes() throws Exception {
		
		BundleParser conv = new BundleParser();
		InputStream is = getClass().getResourceAsStream("/example-messages/fhir/practitionerSearchResults.json");
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String message = writer.toString();
		AtomFeed result = conv.fromJsonOrXml(message);
		Assert.assertNotNull(result);
	}

}
