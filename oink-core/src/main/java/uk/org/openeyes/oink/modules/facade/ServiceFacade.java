package uk.org.openeyes.oink.modules.facade;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hl7.fhir.instance.formats.Composer;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.Parser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKBody;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.OutboundOinkService;
import uk.org.openeyes.oink.messaging.RabbitRoute;
import uk.org.openeyes.oink.security.SecurityService;

/**
 * 
 * @author Oliver Wilkie
 */
@Controller
public class ServiceFacade {

	@Autowired
	SecurityService securityService;

	@Autowired
	RoutingService routingService;

	@Autowired
	OutboundOinkService oinkService;

	private static final Logger logger = LoggerFactory
			.getLogger(ServiceFacade.class);

	public final String[] acceptedContentTypes = { "application/json+fhir" };
	private final Parser fhirParser = new JsonParser();
	private final Composer fhirComposer = new JsonComposer();

	
	@RequestMapping(value = { "/{destinationId}/{fhirResourcePath:.*}" }, method = {
			RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
			RequestMethod.DELETE })
	public void handleRequest(@PathVariable String destinationId,
			@PathVariable String fhirResourcePath,
			WebRequest request, InputStream contentIs, HttpServletRequest originalRequest,
			HttpServletResponse response)
			throws DestinationNotRecognisedException,
			NoRabbitMappingFoundException, RabbitReplyTimeoutException,
			UnsupportedContentTypeException, MalformedFhirResourceException,
			IOException {

		// Method
		HttpMethod requestMethod = HttpMethod.valueOf(originalRequest.getMethod());
		
		// Get origin of request from authenticated user
		Principal authenticatedUser = request.getUserPrincipal();
		String originId = securityService.getOrganisation(authenticatedUser);

		// Check destination is valid
		boolean destinationExists = routingService
				.isDestinationValid(destinationId);
		if (!destinationExists) {
			throw new DestinationNotRecognisedException();
		}

		// Check route exists
		RabbitRoute route = routingService.getRouting(destinationId,
				fhirResourcePath, requestMethod);
		if (route == null) {
			throw new NoRabbitMappingFoundException();
		}

		// Build OINK Request Message
		OINKRequestMessage message = new OINKRequestMessage();
		message.setOrigin(originId);
		message.setDestination(destinationId);
		message.setMethod(requestMethod);
		message.setParameters(request.getParameterMap());
		message.setResourcePath(fhirResourcePath);

		// Build Request Body if required
		OINKBody body = null;
		if (requestMethod.equals(HttpMethod.POST)
				|| requestMethod.equals(HttpMethod.PUT)) {
			String contentType = request.getHeader("Content-Type");
			if (contentType == null
					|| !Arrays.asList(acceptedContentTypes).contains(
							contentType)) {
				throw new UnsupportedContentTypeException();
			}
			try {
				ResourceOrFeed fhirResource = fhirParser
						.parseGeneral(contentIs);
				body = new OINKBody(fhirResource);
			} catch (Exception e) {
				throw new MalformedFhirResourceException();
			}

		}
		message.setBody(body);

		// Send OINK Request Message
		OINKResponseMessage oinkResponse = (OINKResponseMessage) oinkService
				.convertSendAndReceive(route.getRoutingKey(), message);

		if (oinkResponse == null) {
			throw new RabbitReplyTimeoutException("");
		}

		// Prepare REST response
		response.setStatus(oinkResponse.getStatus());
		OutputStream os = response.getOutputStream();
		OINKBody oinkResponseBody = oinkResponse.getBody();
		if (oinkResponseBody != null) {
			try {
				if (oinkResponseBody.getFeed() != null) {
					response.setContentType("application/json+fhir");
					fhirComposer.compose(os, oinkResponseBody.getFeed(), false);
				} else if (oinkResponseBody.getResource() != null) {
					response.setContentType("application/json+fhir");
					fhirComposer.compose(os, oinkResponseBody.getResource(),
							false);

				}
			} catch (Exception e) {
				throw new MalformedFhirResourceException();
			}
		}
		os.close();

		return; // Handles response ourself
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
