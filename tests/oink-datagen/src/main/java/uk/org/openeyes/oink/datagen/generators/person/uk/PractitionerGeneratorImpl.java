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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;

import uk.org.openeyes.oink.datagen.domain.Address;
import uk.org.openeyes.oink.datagen.domain.GP;
import uk.org.openeyes.oink.datagen.domain.GPPractice;
import uk.org.openeyes.oink.datagen.domain.Identifier;
import uk.org.openeyes.oink.datagen.domain.Organisation;
import uk.org.openeyes.oink.datagen.domain.Practitioner;
import uk.org.openeyes.oink.datagen.domain.Telecom;
import uk.org.openeyes.oink.datagen.generators.person.PractitionerGenerator;
import uk.org.openeyes.oink.datagen.generators.person.beans.uk.NHSPractice;
import uk.org.openeyes.oink.datagen.generators.person.beans.uk.NHSPracticeMapping;
import uk.org.openeyes.oink.datagen.generators.person.beans.uk.NHSPractitioner;

/**
 * Generates GPs from NHS data - UK specific data.
 *
 */
public class PractitionerGeneratorImpl implements PractitionerGenerator {

	private List<Practitioner> gps = new ArrayList<Practitioner>();
	private Map<String, Organisation> gpPractices = new HashMap<String, Organisation>();

	private void init() throws IOException {

		List<String> practicesRaw = IOUtils.readLines(getClass()
				.getResourceAsStream("/generator/uk/epraccur.csv"));
		List<String> practitionersRaw = IOUtils.readLines(getClass()
				.getResourceAsStream("/generator/uk/egpcur.csv"));
		List<String> mappingsRaw = IOUtils.readLines(getClass()
				.getResourceAsStream("/generator/uk/epracmem.csv"));

		Map<String, NHSPractice> nps = NHSPractice.convert(practicesRaw);
		Map<String, NHSPractitioner> nprs = NHSPractitioner
				.convert(practitionersRaw);
		List<NHSPracticeMapping> mappings = NHSPracticeMapping
				.convert(mappingsRaw);

		for (NHSPracticeMapping mapping : mappings) {

			NHSPractice practice = nps.getOrDefault(
					mapping.getParentOrganisationCode(), null);
			NHSPractitioner practitioner = nprs.getOrDefault(
					mapping.getPractitionerCode(), null);

			if (practice != null && practitioner != null) {
				practice.getPractioners().add(practitioner);
				practitioner.getPractices().put(practice.getOrganisationCode(),
						practice);
			}
		}
		
		for (NHSPractice np : nps.values()) {
			GPPractice gpr = new GPPractice();
			
			gpr.setIdentifier(new Identifier(GPPractice.getURI(), np
					.getOrganisationCode()));
			gpr.setAddress(new Address());
			gpr.getAddress().setLine1(np.getAddressLine1());
			gpr.getAddress().setLine2(np.getAddressLine2());
			gpr.getAddress().setLine3(np.getAddressLine3());
			gpr.getAddress().setLine4(np.getAddressLine4());
			gpr.getAddress().setLine5(np.getAddressLine5());
			gpr.getAddress().setZipCode(np.getPostcode());
			
			gpr.setTelecom(new Telecom("telephone", np.getContactTelephoneNumber()));
			
			gpr.setName(np.getName());
			
			gpPractices.put(np.getOrganisationCode(), gpr);
			
			
		}

		for (NHSPractitioner npr : nprs.values()) {
			
			// Must have at least one practice
			if(npr.getPractices().keySet().size() < 1) {
				continue;
			}
			
			GP gp = new GP();
			gp.setIdentifier(new Identifier(GP.getURI(), npr
					.getGeneralPractitionerCode()));
			gp.setAddress(new Address());
			gp.getAddress().setLine1(npr.getAddressLine1());
			gp.getAddress().setLine2(npr.getAddressLine2());
			gp.getAddress().setLine3(npr.getAddressLine3());
			gp.getAddress().setLine4(npr.getAddressLine4());
			gp.getAddress().setLine5(npr.getAddressLine5());
			gp.getAddress().setZipCode(npr.getPostcode());
			
			String[] names = npr.getName().split("\\s");
			String firstName = "";
			for(int i = 0; i < names.length; i++) {
				if(i < (names.length - 1)) {
					if(firstName.length() > 0) {
						firstName += " ";
					}
					firstName += names[i];
				}
			}
			
			gp.setLastName(names[names.length - 1]);
			gp.setFirstName(firstName);
			gp.setPrefix("DR");

			for(String key : npr.getPractices().keySet()) {
				gp.getOrganisations().add(gpPractices.get(key));
			}
			
			gps.add(gp);
		}
	}

	public List<Practitioner> generate(int quantity) {

		List<Practitioner> practitioners = new ArrayList<Practitioner>();

		if (gps.size() < 1) {
			try {
				init();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Random rng = new Random();
		for(int i = 0; i < quantity; i++) {
			practitioners.add(gps.get(rng.nextInt(gps.size())));
		}
		
		return practitioners;
	}
}
