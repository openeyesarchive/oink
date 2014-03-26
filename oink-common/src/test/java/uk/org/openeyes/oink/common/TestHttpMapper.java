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
package uk.org.openeyes.oink.common;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;

public class TestHttpMapper {
	
	public static final String path1 = "";
	public static final String path2 = "*";
	public static final String path3 = "Pat*";
	public static final String path4 = "Patient";
	public static final String path5 = "Patient/*";
	public static final String path6 = "Patient/Bob";

	@Test
	public void testReturnsNullIfNoMatchExists() {
		HttpMapper.Builder<Integer> builder = new HttpMapper.Builder<Integer>();
		builder.addMapping(path4, HttpMethod.GET, 4);
		HttpMapper<Integer> matcher = builder.build();
		
		assertNull(matcher.get("Bar", HttpMethod.GET));
		assertNull(matcher.get(path4, HttpMethod.POST));		
	}
	
	@Test
	public void testAlwaysReturnsMostExplicit() {
		HttpMapper.Builder<Integer> builder = new HttpMapper.Builder<Integer>();
		builder.addMapping(path1, HttpMethod.GET, 1);
		builder.addMapping(path2, HttpMethod.GET, 2);
		builder.addMapping(path3, HttpMethod.GET, 3);
		builder.addMapping(path4, HttpMethod.GET, 4);
		builder.addMapping(path5, HttpMethod.GET, 5);
		builder.addMapping(path6, HttpMethod.GET, 6);
		HttpMapper<Integer> matcher = builder.build();
		
		assertEquals(Integer.valueOf(1), matcher.get(path1, HttpMethod.GET));
		assertEquals(Integer.valueOf(2), matcher.get("Appointment", HttpMethod.GET));
		assertEquals(Integer.valueOf(3), matcher.get("Patricia", HttpMethod.GET));
		assertEquals(Integer.valueOf(4), matcher.get("Patient", HttpMethod.GET));
		assertEquals(Integer.valueOf(6), matcher.get("Patient/Bob", HttpMethod.GET));
		assertEquals(Integer.valueOf(5), matcher.get("Patient/B", HttpMethod.GET));
	}
	
	@Test
	public void testSingleEntryWithSingleAsterix() {
		HttpMapper.Builder<Integer> builder = new HttpMapper.Builder<Integer>();
		builder.addMapping("*", HttpMethod.ANY, 2);
		HttpMapper<Integer> matcher = builder.build();
		assertEquals(Integer.valueOf(2), matcher.get("Foo", HttpMethod.GET));
		assertEquals(Integer.valueOf(2), matcher.get("Foo/Bar", HttpMethod.POST));
	}

}
