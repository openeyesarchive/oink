package uk.org.openeyes.oink.modules.facade;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.UrlPathHelper;

import uk.org.openeyes.oink.domain.OINKMessage;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.RabbitRoute;

/**
 * 
 * Takes incoming REST requests and forwards them onwards as RabbitMQ messages.
 * RabbitMQ responses are then mapped to REST responses. Each Facade has a
 * {@link RabbitMapper} which acts as a dictionary to match incoming requests to
 * rabbit routes. Returns 404 if no mapping exists for incoming request
 * 
 * @author Oliver Wilkie
 * 
 */
public class Facade implements Controller {

	private RabbitTemplate template;
	private RabbitMapper mapper;

	@Autowired
	SimpleUrlHandlerMapping mapping;

	public Facade(RabbitTemplate template, RabbitMapper mapper) {
		this.template = template;
		this.mapper = mapper;
	}

	/**
	 * @param servletRequest
	 *            see {@link Controller}
	 * @param servletResponse
	 *            see {@link Controller}
	 * @return null because we build the servlet response ourselves
	 * @throws IOException
	 */
	public ModelAndView handleRequest(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {

		// Obtain the path relative to this controller
		String pathRemainder = "/"
				+ (String) servletRequest
						.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

		// Get the request method type
		HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod()); // GET

		RabbitRoute route = mapper.getMapping(pathRemainder, method);
		if (route == null) {
			// No mapping was found
			servletResponse.setStatus(HttpStatus.NOT_FOUND.value());
		} else {
			// Mapping was found
			OINKMessage message = buildMessage(servletRequest);
			OINKResponseMessage response = (OINKResponseMessage) template
					.convertSendAndReceive(route.getExchange(),
							route.getRoutingKey(), message);
			populateServletResponse(servletResponse, response);
		}
		return null; // Indicate we have handled the request ourselves
	}

	/**
	 * Uses the contents of the {@link OINKResponseMessage} to populate the
	 * servletResponse. There is no manipulation of the contents of the OINK
	 * message although this will probably be needed in the future.
	 * 
	 */
	private void populateServletResponse(HttpServletResponse servletResponse,
			OINKResponseMessage message) {
		try {
			// Set status code
			servletResponse.setStatus(message.getStatus().value());

			// Set headers
			HttpHeaders headers = message.getHeaders();
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				for (String value : entry.getValue()) {
					servletResponse.setHeader(entry.getKey(), value);
				}
			}

			// Copy body with no regard to the body type.
			ByteArrayInputStream in = new ByteArrayInputStream(
					message.getBody());
			ServletOutputStream out = servletResponse.getOutputStream();
			IOUtils.copy(in, out);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);

		} catch (IOException e) {
			servletResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}

	}

	/**
	 * Builds an {@link OINKRequestMessage} encapsulating the incoming REST
	 * request. The OINKRequestMessage acts as a wrapper containing, the REST
	 * message body, the REST message headers and the REST request info i.e.
	 * request path and method. 
	 */
	@SuppressWarnings("unchecked")
	private OINKRequestMessage buildMessage(HttpServletRequest servletRequest) {

		// Get HTML requestedResource

		// http://facadeServer/facadeApp/fhir/Patient/Search?name=Bob
		// the part we need to forward is /Patient/Search?name=Bob
		UrlPathHelper pathHelper = mapping.getUrlPathHelper();
		String destUrl = pathHelper.getPathWithinServletMapping(servletRequest);
		destUrl += "?" + servletRequest.getQueryString();

		// Get HTML method
		HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod()); // e.g.
																			// GET

		// Get HTML headers
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = servletRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			Enumeration<String> headerValues = servletRequest
					.getHeaders(headerName);
			while (headerValues.hasMoreElements()) {
				String headerValue = headerValues.nextElement();
				headers.add(headerName, headerValue);
			}
		}

		// Get HTML body
		byte[] body = null;
		if (servletRequest.getContentLength() > 0) {
			try {
				body = IOUtils.toByteArray(servletRequest.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return new OINKRequestMessage(destUrl, method, headers, body);
	}
}
