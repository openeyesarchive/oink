package uk.org.openeyes.oink.modules.facade;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.formats.Composer;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import com.google.api.client.http.HttpHeaders;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.OINKBody;
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

	private static final Logger logger = LoggerFactory.getLogger(Facade.class);

	private RabbitTemplate template;
	private HttpMapper<RabbitRoute> mapper;
	// private RabbitMapper mapper;

	private Composer hl7JsonComposer;

	@Autowired
	SimpleUrlHandlerMapping mapping;

	public Facade(RabbitTemplate template, HttpMapper<RabbitRoute> mapper) {
		this.template = template;
		this.mapper = mapper;
		hl7JsonComposer = new JsonComposer();
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
			HttpServletResponse servletResponse)
			throws NoRabbitMappingFoundException, RabbitReplyTimeoutException,
			IOException {
		// Obtain the path relative to this controller
		String resource = getPathWithinHandler(servletRequest);

		// Get the request method type
		HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod()); // GET
		
		logger.debug("Received a request for the following resource: "+resource +" via method: " + method);

		RabbitRoute route = mapper.get(resource, method);
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
	public ModelAndView handleNoRabbitMappingFoundException(
			NoRabbitMappingFoundException ex, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
		logger.error("The requested resource under the selected method has no mapping to an OINK Service");
		return new ModelAndView();
	}

	@ExceptionHandler(RabbitReplyTimeoutException.class)
	public ModelAndView handleRabbitReplyTimeoutException(
			RabbitReplyTimeoutException ex, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
		logger.error("The Service on the other side of OINK did not reply in time");
		return new ModelAndView();
	}

	@ExceptionHandler(AmqpConnectException.class)
	public ModelAndView handleAmqpConnectException(AmqpConnectException ex,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
		logger.error("The underlying Rabbit Server could not be reached");
		return new ModelAndView();
	}

	/**
	 * Uses the contents of the {@link OINKResponseMessage} to populate the
	 * servletResponse. There is no manipulation of the contents of the OINK
	 * message although this will probably be needed in the future.
	 * 
	 * @throws IOException
	 * 
	 */
	private void populateServletResponse(HttpServletResponse servletResponse,
			OINKResponseMessage message) throws IOException {
		// Set status code
		servletResponse.setStatus(message.getStatus());

		// Return body of OINK Message
		OutputStream os = servletResponse.getOutputStream();
		try {
			OINKBody body = message.getBody();
			if (body != null) {
				if (body.getFeed() != null) {
					servletResponse.setContentType("application/json+fhir");
					hl7JsonComposer.compose(os, body.getFeed(), true);
				} else if (body.getResource() != null) {
					servletResponse.setContentType("application/json+fhir");
					hl7JsonComposer.compose(os, body.getResource(), true);
				}
			}
		} catch (Exception e) {
			throw new IOException();
		} finally {
			os.close();
		}
	}

	/**
	 * Builds an {@link OINKRequestMessage} encapsulating the incoming REST
	 * request. The OINKRequestMessage acts as a wrapper containing, the REST
	 * message body, the REST message headers and the REST request info i.e.
	 * request path and method.
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private OINKRequestMessage buildMessage(HttpServletRequest servletRequest)
			throws IOException {

		// Get HTML requestedResource

		// http://facadeServer/facadeApp/fhir/Patient/Search?name=Bob
		// the part we need to forward is Patient/Search?name=Bob
		String destUrl = getPathWithinHandler(servletRequest);

		String queryString = servletRequest.getQueryString();
		Map<String, String> params = splitQuery(queryString);

		// Get HTML method
		String method = servletRequest.getMethod(); // e.g.
													// GET

		// Get HTML headers
		HttpHeaders headers = new HttpHeaders();
		Enumeration<String> headerNames = servletRequest.getHeaderNames();
		while (headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			List<String> headerValues = Collections.list(servletRequest
					.getHeaders(headerName));
			headers.set(headerName, headerValues);
		}

		return new OINKRequestMessage(destUrl, method, params, null);
	}

	public static Map<String, String> splitQuery(String query)
			throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		if (query.isEmpty()) {
			return query_pairs;
		}
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
					URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}

	public final HttpMapper<RabbitRoute> getMapper() {
		return mapper;
	}
}
