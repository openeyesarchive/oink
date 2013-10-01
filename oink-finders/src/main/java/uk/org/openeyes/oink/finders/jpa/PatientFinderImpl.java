package uk.org.openeyes.oink.finders.jpa;

import java.util.List;

import javax.inject.Inject;

import uk.org.openeyes.oink.domain.Patient;
import uk.org.openeyes.oink.entity.springdata.PatientEntity;
import uk.org.openeyes.oink.finders.PatientFinder;
import uk.org.openeyes.oink.infrastructure.annotations.Finder;
import uk.org.openeyes.oink.repo.PatientRepo;

@Finder
public class PatientFinderImpl implements PatientFinder {
	
	@Inject
	private PatientRepo repo;
	
	public List<Patient> getPatientsById(String identifierLikePattern) {
		List<PatientEntity> patients = repo.findByIdentifer(identifierLikePattern);
		
		
		return null;
	}
}
