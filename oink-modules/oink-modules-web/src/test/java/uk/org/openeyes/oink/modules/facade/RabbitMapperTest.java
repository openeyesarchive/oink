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


public class RabbitMapperTest {
	
	public final String[] resources = {"/*","/Patient/*","/Patient/Tom"};
	public final String[] methods = {"GET","GET","GET"};
	public final String[] routings = {"route.one","route.two","route.three"};
	public final String[] exchanges = {"exchange.one","exchange.two","exchange.three"};
	
//	private RabbitMapper mapper;
//	
//	@Before
//	public void buildMapper() {
//		RabbitMapperBuilder builder = new RabbitMapperBuilder();
//		for (int i = 0; i < resources.length; i++) {
//			builder.addMapping(resources[i], methods[i], routings[i], exchanges[i]);
//		}
//		mapper = builder.build();		
//	}
//	
//	@Test
//	public void testFindsMostGranular() {
//		RabbitRoute actual = null;
//		RabbitRoute expected = null;
//		actual = mapper.getMapping("/Appointment", HttpMethod.GET);
//		expected = new RabbitRoute(routings[0], exchanges[0]);
//		assertEquals(expected, actual);
//		
//		actual = mapper.getMapping("/Patient", HttpMethod.GET);
//		expected =  new RabbitRoute(routings[0], exchanges[0]);
//		assertEquals(expected, actual);
//		
//		actual = mapper.getMapping("/Patient/To", HttpMethod.GET);
//		expected = new RabbitRoute(routings[1], exchanges[1]);
//		assertEquals(expected, actual);
//		
//		actual = mapper.getMapping("/Patient/Tom", HttpMethod.GET);
//		expected = new RabbitRoute(routings[2], exchanges[2]);
//		assertEquals(expected, actual);
//		
//		actual = mapper.getMapping("/Patient/Tom/Jones", HttpMethod.GET);
//		expected = new RabbitRoute(routings[1], exchanges[1]);
//		assertEquals(expected, actual);
//				
//		
//	}

}
