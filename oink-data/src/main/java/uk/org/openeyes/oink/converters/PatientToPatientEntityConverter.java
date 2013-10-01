package uk.org.openeyes.oink.converters;

import java.util.LinkedHashSet;
import java.util.Locale;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import uk.org.openeyes.oink.domain.Patient;
import uk.org.openeyes.oink.entity.springdata.PatientEntity;
import uk.org.openeyes.oink.entity.springdata.PatientIdentiferEntity;

@Component
public class PatientToPatientEntityConverter implements Converter<Patient, PatientEntity> {

	@Override
	public PatientEntity convert(Patient model) {
		
		PatientEntity entity = new PatientEntity();
		entity.setFamilyName(model.getFamilyName());
		entity.setGivenNames(model.getGivenName());
		entity.setDateOfBirth(model.getDateOfBirth().toCalendar(Locale.getDefault()));
		entity.setIdentifiers(new LinkedHashSet<PatientIdentiferEntity>());
		for(String i : model.getIdentifiers()) {
			PatientIdentiferEntity identifer = new PatientIdentiferEntity();
			identifer.setIdentifier(i);
			identifer.setPatient(entity);
			entity.getIdentifiers().add(identifer);
		}
		
		return entity;
	}

}
