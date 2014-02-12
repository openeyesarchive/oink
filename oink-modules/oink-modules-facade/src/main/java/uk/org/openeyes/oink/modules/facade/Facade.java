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
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
 * Incoming requests to the Facade Server take the following format OPERATION
 * [base]/[type]/[id] {?_format=[mime-type]}
 * 
 * OPERATION is GET/PUT/DELETE/POST [base] is the leading part of the URL that
 * maps to this Facade controller The rest of the request i.e. /[type]/[id]
 * serves as a key that is looked up in the mapper for this Facade.
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
	 * @throws NoRabbitMappingFoundException 
	 * @throws RabbitReplyTimeoutException 
	 * @throws IOException
	 */
	public ModelAndView handleRequest(HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) throws NoRabbitMappingFoundException, RabbitReplyTimeoutException, IOException {

		// Obtain the path relative to this controller
		String resource = getPathWithinHandler(servletRequest);

		// Get the request method type
		HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod()); // GET

		RabbitRoute route = mapper.getMapping(resource, method);
		if (route == null) {
			// No mapping was found
			throw new NoRabbitMappingFoundException(resource, method);
		} else {
			// Mapping was found
			OINKMessage message = buildMessage(servletRequest);
			OINKResponseMessage response = (OINKResponseMessage) template
					.convertSendAndReceive(route.getRoutingKey(), message);
			if (response == null) {
				throw new RabbitReplyTimeoutException("");
			} else {
				populateServletResponse(servletResponse, response);
			}
		}
		return null; // Indicate we have handled the request ourselves
	}

	private String getPathWithinHandler(HttpServletRequest servletRequest) {
		return (String) servletRequest
				.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	}

	@ExceptionHandler(IOException.class)
	public ModelAndView handleIOException(IOException ex,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
		return new ModelAndView();
	}
	
	@ExceptionHandler(NoRabbitMappingFoundException.class)
	public ModelAndView handleNoRabbitMappingFoundException(NoRabbitMappingFoundException ex,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		return new ModelAndView();
	}
	
	@ExceptionHandler(RabbitReplyTimeoutException.class)
	public ModelAndView handleRabbitReplyTimeoutException(NoRabbitMappingFoundException ex,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
		return new ModelAndView();
	}
	
	@ExceptionHandler(AmqpConnectException.class)
	public ModelAndView handleAmqpConnectException(AmqpConnectException ex,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
		return new ModelAndView();
	}

	/**
	 * Uses the contents of the {@link OINKResponseMessage} to populate the
	 * servletResponse. There is no manipulation of the contents of the OINK
	 * message although this will probably be needed in the future.
	 * @throws IOException 
	 * 
	 */
	private void populateServletResponse(HttpServletResponse servletResponse,
			OINKResponseMessage message) throws IOException {
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
		ByteArrayInputStream in = new ByteArrayInputStream(message.getBody());
		ServletOutputStream out = servletResponse.getOutputStream();
		IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);
		IOUtils.closeQuietly(out);
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
		// the part we need to forward is Patient/Search?name=Bob
		String destUrl = getPathWithinHandler(servletRequest);
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
