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
package uk.org.openeyes.oink.datagen.generators.identifier;

import uk.org.openeyes.oink.datagen.generators.BaseFactory;
import uk.org.openeyes.oink.datagen.generators.identifier.uk.NHSNoGeneratorImpl;

/**
 * A factory to retrieve an identifier generator for a region.
 *
 */
public class IdentifierGeneratorFactory {
	
	private static BaseFactory<IdentifierGenerator> factory = new BaseFactory<IdentifierGenerator>() {
		protected IdentifierGenerator createInstance(String key) {
			if(key == "uk") {
				// United Kingdom
				return new NHSNoGeneratorImpl();
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
	
	public synchronized static IdentifierGenerator getInstance(String region) throws Exception {
		return factory.getInstanceReference(region, "Region not supported.");
	}

}
