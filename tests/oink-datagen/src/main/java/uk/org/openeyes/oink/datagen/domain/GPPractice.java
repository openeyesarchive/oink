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
package uk.org.openeyes.oink.datagen.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import uk.org.openeyes.oink.datagen.domain.identifier.NHSNumber;

/**
 * This class represents a GP practice.
 *
 */
public class GPPractice extends Organisation {

	private static String URI = null;
	public static String getURI() {
		if(URI == null) {
			InputStream is = GPPractice.class.getClassLoader().getResourceAsStream("oink-datagen-uk-system-identifiers.properties");
			Properties p = new Properties();
			try {
				p.load(is);
				is.close();
				URI = p.getProperty("oink.datagen.uk.identifiers.gppractice");
			} catch (IOException e) {
				URI = "urn:nhs-uk:ods:gpracc";
			}
		}
		return URI;
	}	
}
