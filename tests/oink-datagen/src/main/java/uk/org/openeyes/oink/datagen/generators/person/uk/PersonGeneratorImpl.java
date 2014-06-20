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
package uk.org.openeyes.oink.datagen.generators.person.uk;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

import uk.org.openeyes.oink.datagen.domain.Address;
import uk.org.openeyes.oink.datagen.domain.Identifier;
import uk.org.openeyes.oink.datagen.domain.Person;
import uk.org.openeyes.oink.datagen.domain.Practitioner;
import uk.org.openeyes.oink.datagen.domain.Telecom;
import uk.org.openeyes.oink.datagen.generators.address.uk.AddressGenerator;
import uk.org.openeyes.oink.datagen.generators.identifier.uk.NHSNoGeneratorImpl;
import uk.org.openeyes.oink.datagen.generators.person.PersonGenerator;

/**
 * Generates patients with UK specific data.
 *
 */
public class PersonGeneratorImpl implements PersonGenerator {
	public List<Person> generate(int quantity) {
		
		if(givenNamesMale == null) {
			try {
				init();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		NHSNoGeneratorImpl nhsNoGenerator = new NHSNoGeneratorImpl();
		PractitionerGeneratorImpl practionerGenerator = new PractitionerGeneratorImpl();
		
		Random rng = new Random();
		List<Person> persons = new ArrayList<Person>();
		for(int i = 0; i < quantity; i++) {
			Person person = new Person();
			
			switch(rng.nextInt() % 2) {
				case 0:
					person.setPrefix("Mr");
					person.setGender("M");
					break;
				default:
					switch(rng.nextInt() % 3) {
					case 0:
						person.setPrefix("Mrs");
						break;
					case 1:
						person.setPrefix("Miss");
						break;
					default:
						person.setPrefix("Ms");
					}
					person.setGender("F");
					break;
			} 
			switch(rng.nextInt() % 15) {
			case 0:
				person.setPrefix("Dr");
				break;
			case 1:
				person.setSuffix("Jr");
				break;
			}
			
			List<String> names = person.getGender().equals("M") ? givenNamesMale : givenNamesFemale;
			
			// set name
			person.setFirstName(names.get(rng.nextInt(names.size())));
			person.setLastName(surnames.get(rng.nextInt(surnames.size())));
			
			// set date of birth
			person.setDateOfBirth(new DateTime().minusYears(rng.nextInt(90)));
			person.setDateOfBirth(new DateTime().minusMonths(rng.nextInt(12)));
			person.setDateOfBirth(new DateTime().minusDays(rng.nextInt(30)));
			
			if((rng.nextInt() % 50) == 0) {
				// person is dead
				person.setDateOfDeath(new DateTime().minusYears(rng.nextInt(20)));
				person.setDateOfDeath(new DateTime().minusMonths(rng.nextInt(12)));
				person.setDateOfDeath(new DateTime().minusDays(rng.nextInt(30)));
				
				if(person.getDateOfDeath().isBefore(person.getDateOfBirth())) {
					person.setDateOfDeath(new DateTime().minusHours(rng.nextInt(4) + 1));
				}
			}
			
			// set inactive
			person.setActive((rng.nextInt() % 50) == 0);
			
			// set hospital number
			person.setIdentifiers(new ArrayList<Identifier>());
			String hosNumString = UUID.randomUUID().toString().replaceAll("\\D", "");
			Identifier hosNum = new Identifier("primary", "hosnum", hosNumString);
			person.getIdentifiers().add(hosNum);
			
			// set NHS no
			person.getIdentifiers().add(nhsNoGenerator.generate(1).get(0));
			
			// set phone numbers
			int telecoms = rng.nextInt(3) + 1;
			person.setTelecoms(new ArrayList<Telecom>());
			for(int j = 0; j < telecoms; j++) {
				Telecom t = new Telecom();
				switch(j) {
				case 0:
					t.setUsage("home");
					break;
				case 1:
					t.setUsage("work");
					break;
				default:
					t.setUsage("other");
					break;
				}
				
				String template = phoneTemplates.get(rng.nextInt(phoneTemplates.size()));
				while(template.contains("#")) {
					template = template.replaceFirst("#", String.valueOf(rng.nextInt(10)));
				}
				t.setValue(template);
				
				person.getTelecoms().add(t);
			}
			
			// set addresses
			int addresses = rng.nextInt(3) + 1;
			person.setAddresses(new ArrayList<Address>());
			for(int j = 0; j < addresses; j++) {
				Address a = addressGenerator.generate(1).get(0);
				switch(j) {
				case 0:
					a.setUsage("home");
					break;
				case 1:
					a.setUsage("correspondence");
					break;
				default:
					a.setUsage("other");
					break;
				}
				
				person.getAddresses().add(a);
			}
			
			Practitioner p = practionerGenerator.generate(1).get(0);
			person.setManagingPractioner(p);
			person.setManagingOrganisation(p.getOrganisations().get(0));
			
			persons.add(person);
		}
		
		return persons;
	}
	
	private List<String> givenNamesMale = null;
	private List<String> givenNamesFemale = null;
	private List<String> surnames = null;
	private List<String> phoneTemplates = null;
	private AddressGenerator addressGenerator = new AddressGenerator();

	private void init() throws IOException, URISyntaxException {
		
		givenNamesMale = IOUtils.readLines(getClass().getResourceAsStream("/generator/uk/uk_firstnames_male.txt"));
		givenNamesFemale = IOUtils.readLines(getClass().getResourceAsStream("/generator/uk/uk_firstnames_female.txt"));
		surnames = IOUtils.readLines(getClass().getResourceAsStream("/generator/uk/uk_surnames.txt"));
		phoneTemplates = IOUtils.readLines(getClass().getResourceAsStream("/generator/uk/uk_phone_templates.txt"));
	}
}
