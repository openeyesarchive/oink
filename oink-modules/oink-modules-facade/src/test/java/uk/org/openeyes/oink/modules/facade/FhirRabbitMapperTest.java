package uk.org.openeyes.oink.modules.facade;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.org.openeyes.oink.domain.HTTPMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;
import uk.org.openeyes.oink.modules.facade.FhirRabbitMapper.FhirRabbitMapperBuilder;

public class FhirRabbitMapperTest {
	
	public final String[] resources = {"/*","/Patient/*","/Patient/Tom"};
	public final String[] methods = {"get","get","get"};
	public final String[] routings = {"route.one","route.two","route.three"};
	public final String[] exchanges = {"exchange.one","exchange.two","exchange.three"};
	
	private FhirRabbitMapper mapper;
	
	@Before
	public void buildMapper() {
		FhirRabbitMapperBuilder builder = new FhirRabbitMapperBuilder();
		for (int i = 0; i < resources.length; i++) {
			builder.addMapping(resources[i], methods[i], routings[i], exchanges[i]);
		}
		mapper = builder.build();		
	}
	
	@Test
	public void testFindsMostGranular() {
		RabbitRoute actual = null;
		RabbitRoute expected = null;
		actual = mapper.getMapping("/Appointment", HTTPMethod.GET);
		expected = new RabbitRoute(routings[0], exchanges[0]);
		assertEquals(expected, actual);
		
		actual = mapper.getMapping("/Patient", HTTPMethod.GET);
		expected =  new RabbitRoute(routings[0], exchanges[0]);
		assertEquals(expected, actual);
		
		actual = mapper.getMapping("/Patient/To", HTTPMethod.GET);
		expected = new RabbitRoute(routings[1], exchanges[1]);
		assertEquals(expected, actual);
		
		actual = mapper.getMapping("/Patient/Tom", HTTPMethod.GET);
		expected = new RabbitRoute(routings[2], exchanges[2]);
		assertEquals(expected, actual);
		
		actual = mapper.getMapping("/Patient/Tom/Jones", HTTPMethod.GET);
		expected = new RabbitRoute(routings[1], exchanges[1]);
		assertEquals(expected, actual);
				
		
	}

}
