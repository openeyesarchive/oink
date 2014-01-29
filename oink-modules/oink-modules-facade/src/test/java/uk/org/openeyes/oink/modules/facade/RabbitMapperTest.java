package uk.org.openeyes.oink.modules.facade;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import uk.org.openeyes.oink.messaging.RabbitRoute;
import uk.org.openeyes.oink.modules.facade.RabbitMapper.RabbitMapperBuilder;

public class RabbitMapperTest {
	
	public final String[] resources = {"/*","/Patient/*","/Patient/Tom"};
	public final String[] methods = {"get","get","get"};
	public final String[] routings = {"route.one","route.two","route.three"};
	public final String[] exchanges = {"exchange.one","exchange.two","exchange.three"};
	
	private RabbitMapper mapper;
	
	@Before
	public void buildMapper() {
		RabbitMapperBuilder builder = new RabbitMapperBuilder();
		for (int i = 0; i < resources.length; i++) {
			builder.addMapping(resources[i], methods[i], routings[i], exchanges[i]);
		}
		mapper = builder.build();		
	}
	
	@Test
	public void testFindsMostGranular() {
		RabbitRoute actual = null;
		RabbitRoute expected = null;
		actual = mapper.getMapping("/Appointment", HttpMethod.GET);
		expected = new RabbitRoute(routings[0], exchanges[0]);
		assertEquals(expected, actual);
		
		actual = mapper.getMapping("/Patient", HttpMethod.GET);
		expected =  new RabbitRoute(routings[0], exchanges[0]);
		assertEquals(expected, actual);
		
		actual = mapper.getMapping("/Patient/To", HttpMethod.GET);
		expected = new RabbitRoute(routings[1], exchanges[1]);
		assertEquals(expected, actual);
		
		actual = mapper.getMapping("/Patient/Tom", HttpMethod.GET);
		expected = new RabbitRoute(routings[2], exchanges[2]);
		assertEquals(expected, actual);
		
		actual = mapper.getMapping("/Patient/Tom/Jones", HttpMethod.GET);
		expected = new RabbitRoute(routings[1], exchanges[1]);
		assertEquals(expected, actual);
				
		
	}

}
