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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.datagen.domain.Identifier;
import uk.org.openeyes.oink.datagen.domain.identifier.NHSNumber;
import uk.org.openeyes.oink.datagen.generators.identifier.IdentifierGenerator;
import uk.org.openeyes.oink.datagen.generators.identifier.IdentifierGeneratorFactory;
import uk.org.openeyes.oink.datagen.generators.identifier.uk.NHSNoGeneratorImpl;

public class TestNHSNumberGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(TestNHSNumberGenerator.class);

	@Test
	public void testDirect() {
		
		List<NHSNumber> nos = null;
		
		nos = NHSNoGeneratorImpl.generateNHSNo(1000);
		assertEquals(1000, nos.size());
		
		for(int i = 0; i < nos.size(); i++) {
			logger.debug(nos.get(i).toString());
		}
	}
	
	@Test
	public void testAbstract() throws Exception {

		IdentifierGenerator g = IdentifierGeneratorFactory.getInstance("uk");
		
		List<Identifier> nos = g.generate(1000);
		assertEquals(1000, nos.size());
		
		for(Identifier i : nos) {
			logger.debug(i.getValue());
			assertEquals(NHSNumber.URI, i.getCodeSystem());
			assertEquals(12, i.getValue().length());
		}
	}
}
