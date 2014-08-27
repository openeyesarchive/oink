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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import uk.org.openeyes.oink.domain.OINKResponseMessage;

public class TestOinkMessageConverter {

	@Test
	public void testCanParseResponseFromOink() throws IOException {
		
		InputStream is = getClass().getResourceAsStream("/example-messages/oinkresponsemessages/searchResults.json");
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String theString = writer.toString();
				
		OinkMessageConverter conv = new OinkMessageConverter();
		
		OINKResponseMessage resp = conv.responseMessageFromJsonString(theString);
		
		Assert.assertNotNull(resp);
	}
	
}
