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
package uk.org.openeyes.oink.test.data;

import java.util.Calendar;
import java.util.HashSet;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.converters.DataConverterService;
import uk.org.openeyes.oink.domain.Patient;
import uk.org.openeyes.oink.entity.springdata.PatientEntity;
import uk.org.openeyes.oink.entity.springdata.PatientIdentiferEntity;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/oink-context-test-data.xml" })
public class PatientDataTest {
	
	//private static Logger LOGGER = LoggerFactory.getLogger(PatientDataTest.class);

	@Inject
	private DataConverterService conversionService;  
	
	@Test
	public void testPatientToPatientEntity() {
		
		PatientEntity entity = createPatient("Joe", "Bloggs", "11111-ABCDEF",
				"78b133ef-fbd6-49cf-bab3-53a0898e7dea");
		
		Patient model = conversionService.convert(entity, Patient.class);
		PatientEntity entity2 = conversionService.convert(model, PatientEntity.class);

		Assert.assertEquals(2, model.getIdentifiers().size());
		Assert.assertEquals(entity.getFamilyName(), entity2.getFamilyName());
		Assert.assertEquals(entity.getDateOfBirth(), entity2.getDateOfBirth());
		Assert.assertEquals(entity.getGivenNames(), entity2.getGivenNames());
		Assert.assertEquals(entity.getIdentifiers().size(), entity2.getIdentifiers().size());

		PatientIdentiferEntity id1 = null;
		PatientIdentiferEntity id2 = null;
		for(int i = 0; i < entity.getIdentifiers().size(); i++) {
			id1 = entity.getIdentifiers().iterator().next();
			id2 = entity2.getIdentifiers().iterator().next();
			
			Assert.assertEquals(id1.getIdentifier(), id2.getIdentifier());
		}
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
