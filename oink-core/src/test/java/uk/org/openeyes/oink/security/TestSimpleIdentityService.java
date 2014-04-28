package uk.org.openeyes.oink.security;

import static org.junit.Assert.*;

import java.security.Principal;

import javax.security.auth.Subject;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
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
