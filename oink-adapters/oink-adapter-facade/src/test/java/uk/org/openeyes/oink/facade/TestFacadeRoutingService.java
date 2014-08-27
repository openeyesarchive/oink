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
package uk.org.openeyes.oink.facade;

import static org.junit.Assert.assertEquals;

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
