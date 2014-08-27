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
package uk.org.openeyes.oink.datagen.generators.person;

import uk.org.openeyes.oink.datagen.generators.BaseFactory;

/**
 * Factory to create a person generator.
 *
 */
public class PersonGeneratorFactory {
	
	private static BaseFactory<PersonGenerator> factory = new BaseFactory<PersonGenerator>() {
		protected PersonGenerator createInstance(String key) {
			if(key == "uk") {
				// United Kingdom
				return new uk.org.openeyes.oink.datagen.generators.person.uk.PersonGeneratorImpl();
			} else if(key == "us") {
				// USA
				
			} else if(key == "au") {
				// Australia
				
			} else if(key == "nz") {
				// New Zealand
				
			} else if(key == "de") {
				// Germany
				
			} else if(key == "nl") {
				// Netherlands
				
			} else if(key == "fr") {
				// France
				
			}
			// ... add more region support here ...
			return null;
		};
	};
	
	public synchronized static PersonGenerator getInstance(String region) throws Exception {
		return factory.getInstanceReference(region, "Region not supported.");
	}
}
