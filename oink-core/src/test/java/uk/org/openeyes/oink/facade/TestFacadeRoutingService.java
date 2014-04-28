package uk.org.openeyes.oink.facade;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.rabbit.RabbitRoute;

public class TestFacadeRoutingService {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testReturnsSameRoutingKeyRegardless() {
		@SuppressWarnings("unchecked")
		HttpMapper<RabbitRoute> mapper = (HttpMapper<RabbitRoute>) Mockito
				.mock(HttpMapper.class);

		SecureRandom random = new SecureRandom();
		for (int i = 0; i < 10; i++) {
			String queryPath = new BigInteger(130, random).toString(32);
			FacadeRoutingService service = new FacadeRoutingService(mapper,
					queryPath);
			String replyKey = service.getReplyRoutingKey("Patient/Bar",
					HttpMethod.GET);
			assertEquals(queryPath, replyKey);
		}

	}

	@Test
	public void testDelegatesWorkToHttpMapper() {
		@SuppressWarnings("unchecked")
		HttpMapper<RabbitRoute> mapper = (HttpMapper<RabbitRoute>) Mockito
				.mock(HttpMapper.class);
		
		String replyRoutingKey = "dsdssd";
		
		FacadeRoutingService service = new FacadeRoutingService(mapper, replyRoutingKey);
		
		String path = "FOO";
		HttpMethod method = HttpMethod.POST;
		
		service.getRouting(path, method);
		
		Mockito.verify(mapper).get(path, method);
		
	}

}
