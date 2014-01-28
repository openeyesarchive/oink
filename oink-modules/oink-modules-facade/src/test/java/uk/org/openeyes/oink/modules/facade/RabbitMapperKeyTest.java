package uk.org.openeyes.oink.modules.facade;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.org.openeyes.oink.domain.HTTPMethod;

public class RabbitMapperKeyTest {
	
	private final String validPathWithWildcard = "/Patient/*";
	private final String validPathWithoutWildcard = "/Patient/123";
	
	@Test
	public void testValidPathWithAnyMethodIsHigherThanWithAllOtherMethods() {
		RabbitMapperKey request = new RabbitMapperKey(validPathWithWildcard, HTTPMethod.ANY);
		for (HTTPMethod method : HTTPMethod.values()) {
			if (method != HTTPMethod.ANY) {
				RabbitMapperKey other = new RabbitMapperKey(validPathWithWildcard, method);
				assertTrue(request.compareTo(other) > 0);
			}
		}
	}
	
	@Test
	public void testPathWithWildCardIsHigherThanNonWildCard() {
		RabbitMapperKey request = new RabbitMapperKey(validPathWithWildcard, HTTPMethod.ANY);
		RabbitMapperKey other = new RabbitMapperKey(validPathWithoutWildcard, HTTPMethod.ANY);
		assertTrue(request.compareTo(other)>0);
	}

	@Test
	public void testWildCardMatchesAgainstRealPath() {
		HTTPMethod method = HTTPMethod.GET;
		RabbitMapperKey request = new RabbitMapperKey(validPathWithWildcard, method);
		assertTrue(request.matches(validPathWithoutWildcard, method));
	}
	
	@Test
	public void testNonWildCardOnlyMatchesExactMatch() {
		HTTPMethod method = HTTPMethod.GET;
		RabbitMapperKey request = new RabbitMapperKey(validPathWithoutWildcard, method);
		assertTrue(request.matches(validPathWithoutWildcard, method));
		assertFalse(request.matches("/Patient/234", method));
	}
	
	@Test
	public void testAMethodOnlyMatchesAgainstSameMethodOrAnyMethod() {
		for (HTTPMethod entryMethod: HTTPMethod.values()) {
			for (HTTPMethod matchMethod: HTTPMethod.values()) {
				RabbitMapperKey request = new RabbitMapperKey(validPathWithoutWildcard, entryMethod);
				boolean match = request.matches(validPathWithoutWildcard, matchMethod);
				if (entryMethod == HTTPMethod.ANY || entryMethod == matchMethod) {
					assertTrue(match);
				} else {
					assertFalse(match);
				}
				
			}
		}
	}

}
