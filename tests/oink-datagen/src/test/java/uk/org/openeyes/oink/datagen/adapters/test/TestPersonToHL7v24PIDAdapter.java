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

package uk.org.openeyes.oink.datagen.adapters.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.datagen.adapters.PersonToHL7v24PIDAdapter;
import uk.org.openeyes.oink.datagen.domain.Person;
import uk.org.openeyes.oink.datagen.generators.person.PersonGenerator;
import uk.org.openeyes.oink.datagen.generators.person.PersonGeneratorFactory;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ADT_A01;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class TestPersonToHL7v24PIDAdapter {
	
	private static Logger logger = LoggerFactory.getLogger(TestPersonToHL7v24PIDAdapter.class);

	@Test
	public void test() throws Exception {

		PersonToHL7v24PIDAdapter adapter = new PersonToHL7v24PIDAdapter();
		
		// Generate a person
		PersonGenerator g = PersonGeneratorFactory.getInstance("uk");
		Person person = g.generate(1).get(0);
		
		// Convert to ADT_A01
		ADT_A01 adt = adapter.convert(person);
		logger.debug(person.toString());
		logger.debug(adt.toString());
		
		assertFalse(StringUtils.isEmpty(adt.getPID().getPatientName(0).getFamilyName().getFn1_Surname().getValue()));
		
		// Load message with HAPI to check
		@SuppressWarnings("resource")
		HapiContext context = new DefaultHapiContext();
		context.setValidationContext(new NoValidation());	// TODO: turn on validator after resolving practice code non-digit character validation rule exception
		Parser p = context.getGenericParser();
		Message m = p.parse(adt.toString());
		
		assertNotNull(m);
		assertEquals("A01", adt.getMSH().getMessageType().getMsg2_TriggerEvent().getValue());
		assertEquals(adt.toString(), m.toString());
		
		// Convert to ADT_A08
		ADT_A01 adt08 = adapter.convertToA08(person);
		logger.debug(adt08.toString());
		
		assertEquals("A08", adt08.getMSH().getMessageType().getMsg2_TriggerEvent().getValue());
	}
	
}
