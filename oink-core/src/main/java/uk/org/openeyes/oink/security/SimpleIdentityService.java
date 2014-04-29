package uk.org.openeyes.oink.security;

import java.security.Principal;

import javax.security.auth.Subject;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Simple Identity Service. Assumes a subject has only one principle which is a
 * {@link UsernamePasswordAuthenticationToken}. Assumes the principle's name is
 * of the format userId@organizationId.
 * 
 * @author Oliver Wilkie
 * 
 */
public class SimpleIdentityService implements IdentityService {

	@Override
	public String getOrganisation(Subject s) {
		if (s == null) {
			return null;
		}

		for (Principal p : s.getPrincipals()) {
			if (p instanceof UsernamePasswordAuthenticationToken) {
				UsernamePasswordAuthenticationToken details = (UsernamePasswordAuthenticationToken) p;
				String name = details.getName();
				String[] parts = name.split("@");
				if (parts.length == 2) {
					return parts[1];
				}
			}
		}
		return null;
	}

	@Override
	public String getUserId(Subject s) {
		for (Principal p : s.getPrincipals()) {
			if (p instanceof UsernamePasswordAuthenticationToken) {
				UsernamePasswordAuthenticationToken details = (UsernamePasswordAuthenticationToken) p;
				String name = details.getName();
				String[] parts = name.split("@");
				if (parts.length == 2) {
					return parts[0];
				}
			}
		}
		return null;
	}

}
