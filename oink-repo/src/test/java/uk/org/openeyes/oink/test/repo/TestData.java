/*******************************************************************************
 * OpenEyes Interop Toolkit
 * Copyright (C) 2013  OpenEyes Foundation (http://www.openeyes.org.uk)
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
 ******************************************************************************/
package uk.org.openeyes.oink.test.repo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.entity.springdata.PatientEntity;
import uk.org.openeyes.oink.entity.springdata.PatientIdentiferEntity;

@Component
public class TestData {

	private List<PatientEntity> patients = new ArrayList<PatientEntity>();

	public TestData() {
		patients.add(createPatient("Joe", "Bloggs", "11111-ABCDEF",
				"78b133ef-fbd6-49cf-bab3-53a0898e7dea"));
		patients.add(createPatient("Jane", "Doe", "22222-GFHIJK",
				"6c36c839-8d50-4610-b939-04be3c34f886"));
		patients.add(createPatient("Jospeh", "Bloggs", "11111-XXXXX",
				"c0413e52-a4ea-4325-b184-819389956b28"));
	}

	public List<PatientEntity> getPatients() {
		return patients;
	}

	public void setPatients(List<PatientEntity> patients) {
		this.patients = patients;
	}
	
	private static PatientEntity createPatient(String givenName, String familyName, String ident1, String ident2) {
		PatientEntity patient = new PatientEntity();
		patient.setFamilyName(familyName);
		patient.setGivenNames(givenName);
		patient.setDateOfBirth(Calendar.getInstance());
		patient.setIdentifiers(new HashSet<PatientIdentiferEntity>());
		PatientIdentiferEntity id1 = new PatientIdentiferEntity();
		id1.setIdentifier(ident1);
		id1.setPatient(patient);
		patient.getIdentifiers().add(id1);
		PatientIdentiferEntity id2 = new PatientIdentiferEntity();
		id2.setIdentifier(ident2);
		id2.setPatient(patient);
		patient.getIdentifiers().add(id2);
		return patient;
	}

}
