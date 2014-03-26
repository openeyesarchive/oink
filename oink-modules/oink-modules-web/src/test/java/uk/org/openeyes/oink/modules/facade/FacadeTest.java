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

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import com.google.api.client.http.HttpStatusCodes;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKBody;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.OutboundOinkService;
import uk.org.openeyes.oink.messaging.RabbitRoute;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class FacadeTest {

	Facade facade;

	OutboundOinkService rabbitTemplate;

	HttpMapper<RabbitRoute> mapper;

	OINKResponseMessage mockResponseMessage;

	@Before
	@SuppressWarnings("unchecked") 
	public void setUp() {
		mapper = mock(HttpMapper.class);
		rabbitTemplate = mock(OutboundOinkService.class);
		facade = new ServiceFacade("",mapper);
		facade.setOinkService(rabbitTemplate);
	}
	
	
	@Test
	public void testValidGetRequestAndValidRabbitResponseGivesOkCode()
			throws Exception {
		
		String resourceOnRemoteSystem = "Patient/123";

		// Build request
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");		
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, resourceOnRemoteSystem);
		request.setQueryString("");
		
		// Mock Return Oink Message
		when(mapper.get(resourceOnRemoteSystem, HttpMethod.GET)).thenReturn(
				new RabbitRoute("", ""));
		mockResponseMessage = new OINKResponseMessage(HttpStatusCodes.STATUS_CODE_OK,
				new OINKBody());
		when(
				rabbitTemplate.convertSendAndReceive(anyString(),
						anyObject())).thenReturn(mockResponseMessage);

		// Build expected response
		MockHttpServletResponse response = new MockHttpServletResponse();

		facade.handleRequest(request, response);

		assertEquals(HttpStatusCodes.STATUS_CODE_OK, response.getStatus());
	}

	@Test
	public void testGetRequestWithErrorGetsPassedBackWithError() throws NoRabbitMappingFoundException, RabbitReplyTimeoutException, IOException {
		
		String resourceOnRemoteSystem = "Patient/123";

		// Build request
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");		
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, resourceOnRemoteSystem);
		request.setQueryString("");
		
		// Mock Return Oink Message
		when(mapper.get(resourceOnRemoteSystem, HttpMethod.GET)).thenReturn(
				new RabbitRoute("", ""));
		mockResponseMessage = new OINKResponseMessage(HttpStatusCodes.STATUS_CODE_FORBIDDEN,
				new OINKBody());
		when(rabbitTemplate.convertSendAndReceive(anyString(),
						anyObject())).thenReturn(mockResponseMessage);

		// Build expected response
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		facade.handleRequest(request, response);
		assertEquals(HttpStatusCodes.STATUS_CODE_FORBIDDEN, response.getStatus());

	}

	@Test(expected=NoRabbitMappingFoundException.class)
	public void testInvalidGetRequest() throws NoRabbitMappingFoundException, RabbitReplyTimeoutException, IOException {
		// Build request
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");		
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, "NULL");


		mockResponseMessage = new OINKResponseMessage(HttpStatusCodes.STATUS_CODE_OK,
				new OINKBody());
		when(
				rabbitTemplate.convertSendAndReceive(anyString(), anyString(),
						anyObject())).thenReturn(mockResponseMessage);

		// Build expected response
		MockHttpServletResponse response = new MockHttpServletResponse();

		facade.handleRequest(request, response);

	}

}
