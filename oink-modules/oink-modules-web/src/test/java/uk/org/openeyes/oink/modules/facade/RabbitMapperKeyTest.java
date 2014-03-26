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
