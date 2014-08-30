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
package uk.org.openeyes.oink.datagen.generators.identifier.uk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import uk.org.openeyes.oink.datagen.domain.Identifier;
import uk.org.openeyes.oink.datagen.domain.identifier.NHSNumber;
import uk.org.openeyes.oink.datagen.generators.identifier.IdentifierGenerator;

/**
 * NHS number generator.
 *
 */
public class NHSNoGeneratorImpl implements IdentifierGenerator {
	
	public static List<NHSNumber> generateNHSNo(int quantity)  {
		List<NHSNumber> nos = new ArrayList<NHSNumber>();
		
		Random rng = new Random();
		
		Long i = 0L;
		while(i < quantity) {
			Long nhsNoInt;
			nhsNoInt = Math.abs(rng.nextLong());
			
			String nhsNoString = String.valueOf(nhsNoInt);
			if(nhsNoString.length() >= 9) {
				nhsNoString.substring(0, 9);
			
				NHSNumber nhsNo = new NHSNumber(nhsNoString);
				if(nhsNo.isValid()) {
					nos.add(nhsNo);
					i++;
				}
			}
		}
		
		return nos;
	}

	@Override
	public List<Identifier> generate(int quantity) {
		
		List<Identifier> nos = new ArrayList<Identifier>();
		
		List<NHSNumber> nhsNos = generateNHSNo(quantity);
		for(NHSNumber nhsNo : nhsNos) {
			nos.add(new Identifier(NHSNumber.getURI(), nhsNo.toString()));
		}
		
		return nos;
	}
}
