package uk.org.openeyes.oink.security;

import javax.security.auth.Subject;

public interface IdentityService {

	public String getOrganisation(Subject s);
	public String getUserId(Subject s);
	
}
