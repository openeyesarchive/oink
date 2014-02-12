package uk.org.openeyes.oink.modules.facade;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

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
	RabbitMapper mapper;

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

		// Mock Return Oink Message
		when(mapper.getMapping(resourceOnRemoteSystem, HttpMethod.GET)).thenReturn(
				new RabbitRoute("", ""));
		mockResponseMessage = new OINKResponseMessage(HttpStatus.OK,
				new HttpHeaders(), new byte[0]);
		when(
				rabbitTemplate.convertSendAndReceive(anyString(),
						anyObject())).thenReturn(mockResponseMessage);

		// Build expected response
		MockHttpServletResponse response = new MockHttpServletResponse();

		facade.handleRequest(request, response);

		assertEquals(HttpStatus.OK.value(), response.getStatus());
	}

	@Test
	public void testGetRequestWithErrorGetsPassedBackWithError() throws NoRabbitMappingFoundException, RabbitReplyTimeoutException, IOException {
		
		String resourceOnRemoteSystem = "Patient/123";

		// Build request
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");		
		request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, resourceOnRemoteSystem);

		// Mock Return Oink Message
		when(mapper.getMapping(resourceOnRemoteSystem, HttpMethod.GET)).thenReturn(
				new RabbitRoute("", ""));
		mockResponseMessage = new OINKResponseMessage(HttpStatus.FORBIDDEN,
				new HttpHeaders(), new byte[0]);
		when(rabbitTemplate.convertSendAndReceive(anyString(),
						anyObject())).thenReturn(mockResponseMessage);

		// Build expected response
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		facade.handleRequest(request, response);
		assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

	}

	@Test
	public void testInvalidGetRequest() throws NoRabbitMappingFoundException, RabbitReplyTimeoutException, IOException {
		// Build request
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setMethod("GET");
		request.setPathInfo("/Patient");

		mockResponseMessage = new OINKResponseMessage(HttpStatus.OK,
				new HttpHeaders(), new byte[0]);
		when(
				rabbitTemplate.convertSendAndReceive(anyString(), anyString(),
						anyObject())).thenReturn(mockResponseMessage);

		// Build expected response
		MockHttpServletResponse response = new MockHttpServletResponse();

		facade.handleRequest(request, response);

		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

	}

}
