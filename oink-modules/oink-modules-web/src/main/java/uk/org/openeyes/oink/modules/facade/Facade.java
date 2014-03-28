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
import java.io.InputStream;
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
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.Parser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import com.google.api.client.http.HttpHeaders;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKBody;
import uk.org.openeyes.oink.domain.OINKMessage;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.OutboundOinkService;
import uk.org.openeyes.oink.messaging.RabbitRoute;

/**
 * 
 * A RESTful Gateway to OINK infrastrucure.
 * 
 * Takes incoming REST requests and forwards them onwards as RabbitMQ messages.
 * The Facade then waits for a set period of time for a response RabbitMQ
 * message which is then returned as an HTTP response. 
 * 
 * Each Facade has a {@link RabbitMapper} which
 * acts as a dictionary to match incoming FHIR requests to rabbit routes. 
 * Returns 404 if no mapping exists for incoming request
 * 
 * See implementations for examples of requests.
 * 
 * At the moment all incoming REST requests will expect an RPC reply from a
 * Rabbit message consumer. For example, a POST will expect a consumer to
 * response with a CREATED code.
 * 
 * @author Oliver Wilkie
 * 
 */
public abstract class Facade implements Controller {

	private static final Logger logger = LoggerFactory.getLogger(Facade.class);

	protected HttpMapper<RabbitRoute> resourceToRabbitRouteMapper;
	private final Composer fhirJsonComposer = new JsonComposer();
	private final Parser fhirJsonParser = new JsonParser();

	@Autowired
	OutboundOinkService rabbitService;

	public Facade(HttpMapper<RabbitRoute> mapper) {
		this.resourceToRabbitRouteMapper = mapper;
	}

	/**
	 * Returns the portion of the path between the application context name and
	 * the start of a FHIR resource
	 * 
	 * @return
	 */
	public abstract String getFhirBase();

	protected abstract String getDestinationService(
			HttpServletRequest servletRequest);

	public RabbitRoute getRoute(String resource, HttpMethod method,
			HttpServletRequest servletRequest) {
		return resourceToRabbitRouteMapper.get(resource, method);
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
		String resource = getFhirPartFromPath(servletRequest);

		// Get the request method type
		HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod());

		String infoMessage = String
				.format("Facade with FHIR base %s received a request for resource: %s , method: %s",
						getFhirBase(), resource, method);
		logger.debug(infoMessage);

		RabbitRoute route = getRoute(resource, method, servletRequest);
		if (route == null) {
			// No mapping was found
			throw new NoRabbitMappingFoundException(resource, method);
		} else {
			// Mapping was found
			OINKMessage message = buildMessage(servletRequest);
			OINKResponseMessage response = (OINKResponseMessage) rabbitService
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

	protected abstract String getFhirPartFromPath(
			HttpServletRequest servletRequest);

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
					fhirJsonComposer.compose(os, body.getFeed(), true);
				} else if (body.getResource() != null) {
					servletResponse.setContentType("application/json+fhir");
					fhirJsonComposer.compose(os, body.getResource(), true);
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
		
		// Get HTML Body
		OINKBody body = null;
		try {
			if (servletRequest.getContentLength() > 0) {
				InputStream is = servletRequest.getInputStream();
				ResourceOrFeed contents = fhirJsonParser.parseGeneral(is);
				body = new OINKBody(contents);
			}
		} catch (Exception e) {
			
		}
		
		// Get destination service from path
		return new OINKRequestMessage("", destUrl, method, params, body);
	}

	public static Map<String, String> splitQuery(String query)
			throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		if (query == null || query.isEmpty()) {
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
		return resourceToRabbitRouteMapper;
	}

	public List<Pair<String, HttpMethod>> getResources() {
		return resourceToRabbitRouteMapper.getHttpKey();
	}

	public void setOinkService(OutboundOinkService service) {
		this.rabbitService = service;
	}
	
	/*
	 * Exception Handlers 
	 */
	
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
		response.sendError(HttpServletResponse.SC_ACCEPTED);
		logger.warn("The request was pushed onto the Rabbit broker system but no response was returned in time. The request might or might not eventually be acted upon.");
		return new ModelAndView();
	}
	
	@ExceptionHandler(AmqpException.class)
	public ModelAndView handleAmqpException(AmqpConnectException ex,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
		logger.error("An error occured pushing placing the request on the Rabbit broker system");
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
}
