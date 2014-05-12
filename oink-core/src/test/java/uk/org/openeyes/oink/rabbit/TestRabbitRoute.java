package uk.org.openeyes.oink.rabbit;
import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.org.openeyes.oink.rabbit.RabbitRoute;


public class TestRabbitRoute {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void testCloneConstructor() {
		RabbitRoute route1 = new RabbitRoute("routingKey", "exchange");
		RabbitRoute route2 = new RabbitRoute(route1);
		assertEquals(route1, route2);
	}

	@Test
	public void testGetRoutingKey() {
		RabbitRoute route = new RabbitRoute("routingKey", "exchange");
		assertEquals("routingKey", route.getRoutingKey());
	}

	@Test
	public void testGetExchange() {
		RabbitRoute route = new RabbitRoute("routingKey", "exchange");
		assertEquals("exchange", route.getExchange());
	}
}
