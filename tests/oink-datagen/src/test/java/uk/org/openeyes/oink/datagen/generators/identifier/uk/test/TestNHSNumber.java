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

package uk.org.openeyes.oink.datagen.generators.identifier.uk.test;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.org.openeyes.oink.datagen.domain.identifier.NHSNumber;

public class TestNHSNumber {
	
	@Test
	public void testValidate() {
		
		NHSNumber nhsNo = null;
		
		nhsNo = new NHSNumber("401 023 2137");
		assertTrue(nhsNo.isValid());
		
		nhsNo = new NHSNumber();
		nhsNo.setValue("401 023 2137");
		assertTrue(nhsNo.isValid());
		
		nhsNo = new NHSNumber("123-456-7881");
		assertTrue(nhsNo.isValid());

		nhsNo = new NHSNumber("123456789");
		assertFalse(nhsNo.isValid());

		nhsNo = new NHSNumber();
		nhsNo.setValue("123456789");
		assertFalse(nhsNo.isValid());
		
		nhsNo = new NHSNumber("1234567891");
		assertFalse(nhsNo.isValid());
		
		nhsNo = new NHSNumber("1234567890");
		assertFalse(nhsNo.isValid());

		nhsNo = new NHSNumber();
		assertFalse(nhsNo.isValid());
	}
	
	@Test
	public void testToString() {
		
		NHSNumber nhsNo = null;
		
		nhsNo = new NHSNumber("123-456-7881");
		assertEquals("123-456-7881", nhsNo.toString());

		nhsNo = new NHSNumber("1234567891");
		assertEquals("Invalid value '1234567891'", nhsNo.toString());

		nhsNo = new NHSNumber("123-456-7891");
		assertEquals("Invalid value '1234567891'", nhsNo.toString());
	}
}
