/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
