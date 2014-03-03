package uk.org.openeyes.oink.modules.facade;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpStatusCodes;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.OINKBody;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.RabbitRoute;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class FacadeTest {

	@InjectMocks
	Facade facade;

	@Mock
	RabbitTemplate rabbitTemplate;

	@Mock
	HttpMapper<RabbitRoute> mapper;

	OINKResponseMessage mockResponseMessage;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
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
