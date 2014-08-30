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
package uk.org.openeyes.oink.datagen.generators.person.uk.test;

import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.datagen.domain.Person;
import uk.org.openeyes.oink.datagen.generators.person.PersonGenerator;
import uk.org.openeyes.oink.datagen.generators.person.PersonGeneratorFactory;
import uk.org.openeyes.oink.datagen.generators.person.beans.uk.NHSDateFormatter;

public class TestPersonGenerator {

	private static Logger logger = LoggerFactory.getLogger(TestPersonGenerator.class);
	
	@Test
	public void test() throws Exception {
		PersonGenerator g = PersonGeneratorFactory.getInstance("uk");
		
		List<Person> persons = g.generate(10000);
		
		for(Person person : persons) {
			Assert.assertNotNull(person.getFirstName());
			Assert.assertNotNull(person.getLastName());
			
			logger.debug(person.toString());
		}
	}
	
	@Test
	public void testDateParse() {
		
		DateTime dt = DateTime.parse("19800101", NHSDateFormatter.formatter);
		Assert.assertEquals("01/01/1980", dt.toString("dd/MM/YYYY"));
		
		dt = NHSDateFormatter.parse("19800101");
		Assert.assertEquals("01/01/1980", dt.toString("dd/MM/YYYY"));
		
	}
}
