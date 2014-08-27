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

package uk.org.openeyes.oink.datagen.adapters;

import java.io.IOException;

import org.springframework.core.convert.converter.Converter;

import uk.org.openeyes.oink.datagen.adapters.utils.PersonToHL7v24Helpers;
import uk.org.openeyes.oink.datagen.domain.Person;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v24.message.ADT_A01;

public class PersonToHL7v24PIDAdapter implements Converter<Person, ADT_A01 > {

	@Override
	public ADT_A01 convert(Person person) {
		return convert(person, "A01");
	}
	
	public ADT_A01 convertToA08(Person person) {
		return convert(person, "A08");
	}
	
	protected ADT_A01 convert(Person person, String adtType) {
		ADT_A01 adt = new ADT_A01();
		try {
			adt.initQuickstart("ADT", adtType, "P");
			
			// PID segment
			PersonToHL7v24Helpers.setPID(adt.getPID(), person);
			
			// PD1 segment
			PersonToHL7v24Helpers.setPD1(adt.getPD1(), person);
			
		} catch (HL7Exception e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return adt;
	}
}
