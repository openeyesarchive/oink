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
