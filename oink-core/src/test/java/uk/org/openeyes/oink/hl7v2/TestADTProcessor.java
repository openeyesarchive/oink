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

import org.junit.Assert;
import org.junit.Test;

public class TestADTProcessor {
	
	@Test
	public void testExtractRelativeUrlFromLocation() {
		String location = "http://192.168.1.101:80/api/Practitioner/gp-4/_history/1401788329";
		
		String relative = ADTProcessor.extractResourceRelativeUrlFromLocation(location, "Practitioner");
		
		Assert.assertEquals("Practitioner/gp-4",relative);
	}

}
