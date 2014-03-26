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
