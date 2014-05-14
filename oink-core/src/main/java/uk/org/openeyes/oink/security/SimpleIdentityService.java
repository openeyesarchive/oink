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
