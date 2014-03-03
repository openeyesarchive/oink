package uk.org.openeyes.oink.common;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpMethod;

import uk.org.openeyes.oink.common.HttpMapper;

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

}
