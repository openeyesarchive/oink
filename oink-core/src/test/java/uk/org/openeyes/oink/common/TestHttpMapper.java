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
