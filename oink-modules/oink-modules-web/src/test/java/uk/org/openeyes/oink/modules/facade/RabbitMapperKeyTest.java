package uk.org.openeyes.oink.modules.facade;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpMethod;

public class RabbitMapperKeyTest {
	
	private final String validPathWithWildcard = "/Patient/*";
	private final String validPathWithoutWildcard = "/Patient/123";
	
	@Test
	public void testValidPathWithAnyMethodIsHigherThanWithAllOtherMethods() {
		RabbitMapperKey request = new RabbitMapperKey(validPathWithWildcard);
		for (HttpMethod method : HttpMethod.values()) {
				RabbitMapperKey other = new RabbitMapperKey(validPathWithWildcard, method);
				assertTrue(request.compareTo(other) > 0);
		}
	}
	
	@Test
	public void testPathWithWildCardIsHigherThanNonWildCard() {
		RabbitMapperKey request = new RabbitMapperKey(validPathWithWildcard);
		RabbitMapperKey other = new RabbitMapperKey(validPathWithoutWildcard);
		assertTrue(request.compareTo(other)>0);
	}

	@Test
	public void testWildCardMatchesAgainstRealPath() {
		HttpMethod method = HttpMethod.GET;
		RabbitMapperKey request = new RabbitMapperKey(validPathWithWildcard, method);
		assertTrue(request.matches(validPathWithoutWildcard, method));
	}
	
	@Test
	public void testNonWildCardOnlyMatchesExactMatch() {
		HttpMethod method = HttpMethod.GET;
		RabbitMapperKey request = new RabbitMapperKey(validPathWithoutWildcard, method);
		assertTrue(request.matches(validPathWithoutWildcard, method));
		assertFalse(request.matches("/Patient/234", method));
	}
	
	@Test
	public void testAMethodOnlyMatchesAgainstSameMethodOrAnyMethod() {
		for (HttpMethod entryMethod: HttpMethod.values()) {
			for (HttpMethod matchMethod: HttpMethod.values()) {
				RabbitMapperKey request = new RabbitMapperKey(validPathWithoutWildcard, entryMethod);
				boolean match = request.matches(validPathWithoutWildcard, matchMethod);
				if (entryMethod == null || entryMethod == matchMethod) {
					assertTrue(match);
				} else {
					assertFalse(match);
				}
				
			}
		}
	}

}
