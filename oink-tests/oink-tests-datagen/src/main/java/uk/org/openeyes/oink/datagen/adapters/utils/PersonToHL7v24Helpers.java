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

package uk.org.openeyes.oink.datagen.adapters.utils;

import uk.org.openeyes.oink.datagen.domain.Address;
import uk.org.openeyes.oink.datagen.domain.Identifier;
import uk.org.openeyes.oink.datagen.domain.Person;
import uk.org.openeyes.oink.datagen.domain.Practitioner;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v24.segment.PD1;
import ca.uhn.hl7v2.model.v24.segment.PID;

public class PersonToHL7v24Helpers {

	public static void setPD1(PD1 pd1, Person person)
			throws DataTypeException {
				pd1.getPd13_PatientPrimaryFacility(0).getXon1_OrganizationName().setValue(person.getManagingOrganisation().getName());
				pd1.getPd13_PatientPrimaryFacility(0).getXon3_IDNumber().setValue(person.getManagingOrganisation().getIdentifier().getValue());
				pd1.getPd13_PatientPrimaryFacility(0).getXon7_IdentifierTypeCode().setValue(person.getManagingOrganisation().getIdentifier().getCodeSystem());
				
				Practitioner prac = person.getManagingPractioner();
				pd1.getPd14_PatientPrimaryCareProviderNameIDNo(0).getXcn1_IDNumber().setValue(prac.getIdentifier().getValue());
				pd1.getPd14_PatientPrimaryCareProviderNameIDNo(0).getXcn13_IdentifierTypeCode().setValue(prac.getIdentifier().getCodeSystem());
				pd1.getPd14_PatientPrimaryCareProviderNameIDNo(0).getXcn2_FamilyName().getFn1_Surname().setValue(prac.getLastName());
				pd1.getPd14_PatientPrimaryCareProviderNameIDNo(0).getXcn3_GivenName().setValue(prac.getFirstName());
				pd1.getPd14_PatientPrimaryCareProviderNameIDNo(0).getXcn6_PrefixEgDR().setValue(prac.getPrefix());
				pd1.getPd14_PatientPrimaryCareProviderNameIDNo(0).getXcn5_SuffixEgJRorIII().setValue(prac.getSuffix());
			}

	public static void setPID(PID pid, Person person)
			throws DataTypeException {
				pid.getPatientName(0).getFamilyName().getSurname().setValue(person.getLastName());
				pid.getPatientName(0).getGivenName().setValue(person.getFirstName());
				pid.getPatientName(0).getPrefixEgDR().setValue(person.getPrefix());
				pid.getPatientName(0).getSuffixEgJRorIII().setValue(person.getSuffix());
				
				pid.getAdministrativeSex().setValue(person.getGender());
				
				pid.getDateTimeOfBirth().getTs1_TimeOfAnEvent().setDatePrecision(person.getDateOfBirth().getYear(), person.getDateOfBirth().getMonthOfYear(), person.getDateOfBirth().getDayOfMonth());
				
				for(int i = 0; i < person.getIdentifiers().size(); i++) {
					Identifier identifier = person.getIdentifiers().get(i);
					pid.getPatientIdentifierList(i).getCx1_ID().setValue(identifier.getValue());
					pid.getPatientIdentifierList(i).getCx5_IdentifierTypeCode().setValue(identifier.getCodeSystem());
				}
				
				Address addr = person.getAddresses().get(0);
				pid.getPatientAddress(0).getXad1_StreetAddress().getSad1_StreetOrMailingAddress().setValue(addr.getLine1());
				pid.getPatientAddress(0).getXad2_OtherDesignation().setValue(addr.getLine2());
				pid.getPatientAddress(0).getXad4_StateOrProvince().setValue(addr.getLine3());
				pid.getPatientAddress(0).getCity().setValue(addr.getLine4());
				pid.getPatientAddress(0).getStateOrProvince().setValue(addr.getLine5());
				pid.getPatientAddress(0).getCountry().setValue(addr.getCountry());
				pid.getPatientAddress(0).getZipOrPostalCode().setValue(addr.getZipCode());
			}

	public PersonToHL7v24Helpers() {
		super();
	}

}