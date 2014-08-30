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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.security.auth.Subject;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class TestSimpleIdentityService {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetOrganizationWorksForValidSubject() {
		SimpleIdentityService identityService = new SimpleIdentityService();
		Subject s = new Subject();
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("bob@moorfields", "password");
		s.getPrincipals().add(token);
		
		String organisation = identityService.getOrganisation(s);
		
		String expectedOrganisation = "moorfields";
		assertEquals(expectedOrganisation, organisation);
	}
	
	@Test
	public void testGetOrganizationReturnsNullForSubjectWithMissingPrincipal() {
		SimpleIdentityService identityService = new SimpleIdentityService();
		Subject s = new Subject();
		
		String organisation = identityService.getOrganisation(s);
		assertNull(organisation);
	}

	
	@Test
	public void testGetOrganizationReturnsNullForNullSubject() {
		SimpleIdentityService identityService = new SimpleIdentityService();
		Subject s = null;
		
		String organisation = identityService.getOrganisation(s);
		assertNull(organisation);
	}
	
	@Test
	public void testGetUserIdForValidSubject() {
		SimpleIdentityService identityService = new SimpleIdentityService();
		Subject s = new Subject();
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("bob@moorfields", "password");
		s.getPrincipals().add(token);
		
		String user = identityService.getUserId(s);
		
		String expectedUser = "bob";
		assertEquals(expectedUser, user);		
	}
	
}
