package uk.org.openeyes.oink.modules.facade;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.HandlerMapping;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.messaging.RabbitRoute;

/**
 * A Facade that handles requests for FHIR Resources of a specific service/destination.
 * 
 * <domain>/<appContext>/serviceName/resource
 * e.g. https://www.success.com/oink/ucc1/resource
 * 
 * Note that any wildcards (*) inside the routing keys in mappings will be replaced by the service name
 * e.g. success.referral.* will be translated to success.referral.ucc1 if the request was for ucc1.
 * 
 */
public class ServiceFacade extends Facade {
	
	private final String serviceName;

	public ServiceFacade(String service, HttpMapper<RabbitRoute> mapper) {
		super(mapper);
		this.serviceName = service;
	}

	@Override
	public String getFhirBase() {
		return "/"+serviceName+"/";
	}

	@Override
	protected String getFhirPartFromPath(HttpServletRequest servletRequest) {
		String pathWithinHandlerMapping = (String) servletRequest
				.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		return pathWithinHandlerMapping;
	}

	@Override
	protected String getDestinationService(HttpServletRequest servletRequest) {
		return serviceName;
	}

}
