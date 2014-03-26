package uk.org.openeyes.oink.modules.facade;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.messaging.RabbitRoute;

/**
 * A Facade that handles requests for resources in requests that have no service specified.
 * 
 * i.e. <domain>/<appContext>/resource..
 */
public class NoServiceFacade extends Facade {

	public NoServiceFacade(HttpMapper<RabbitRoute> mapper) {
		super(mapper);
	}

	@Override
	public String getFhirBase() {
		return "/";
	}

	@Override
	protected String getFhirPartFromPath(HttpServletRequest servletRequest) {
		String pathWithinHandler = (String) servletRequest
				.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return pathWithinHandler.substring(pathWithinHandler.indexOf("/")+1);
	}

	@Override
	protected String getDestinationService(HttpServletRequest servletRequest) {
		return null;
	}

}
