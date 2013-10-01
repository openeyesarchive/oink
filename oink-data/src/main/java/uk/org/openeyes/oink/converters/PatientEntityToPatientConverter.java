package uk.org.openeyes.oink.converters;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.domain.Patient;
import uk.org.openeyes.oink.entity.springdata.PatientEntity;
import uk.org.openeyes.oink.entity.springdata.PatientIdentiferEntity;

@Component
public class PatientEntityToPatientConverter implements Converter<PatientEntity, Patient> {

	@Override
	public Patient convert(PatientEntity entity) {
		
		Patient model = new Patient();
		model.setFamilyName(entity.getFamilyName());
		model.setGivenName(entity.getGivenNames());
		model.setDateOfBirth(new DateTime(entity.getDateOfBirth().getTime()));
		model.setIdentifiers(new ArrayList<String>());
		for(PatientIdentiferEntity i : entity.getIdentifiers()) {
			model.getIdentifiers().add(i.getIdentifier());
		}
		
		return model;
	}

}
