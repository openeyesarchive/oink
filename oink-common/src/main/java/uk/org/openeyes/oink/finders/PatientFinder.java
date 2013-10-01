package uk.org.openeyes.oink.finders;

import java.util.List;

import uk.org.openeyes.oink.domain.Patient;

public interface PatientFinder {
	List<Patient> getPatientsById(String identifierLikePattern);
}
