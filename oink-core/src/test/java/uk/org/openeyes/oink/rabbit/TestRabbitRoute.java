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
