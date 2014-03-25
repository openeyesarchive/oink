package uk.org.openeyes.oink.modules.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * 
 * Routes incoming requests to the correct facade.
 * 
 * If facade F has a service name and resources r1, r2
 * http://domain/<serviceName>/r1
 * http://domain/<serviceName>/r2
 * Will map to Facade
 * 
 * If facade F has no service name and resource r1, r2
 * http://domain/r1
 * Will map to Facade
 * 
 * Note 
 * 
 * @author Oliver Wilkie
 */
public class SimpleFacadeHandlerMapping extends SimpleUrlHandlerMapping {
	
	private final static Logger log = LoggerFactory.getLogger(SimpleFacadeHandlerMapping.class);

	private final List<Facade> facades;

	public SimpleFacadeHandlerMapping(List<Facade> facades) {
		super();
		this.facades = facades;
		Map<String, Object> mappings = new HashMap<String, Object>();
		for (Facade facade : facades) {
			StringBuilder facadeBaseSb = new StringBuilder();
			facadeBaseSb.append("/");
			if (facade.hasServiceName()) {
				facadeBaseSb.append(facade.getServiceName());
				facadeBaseSb.append("/");
			}
			facadeBaseSb.append("**");
			if (mappings.containsKey(facadeBaseSb.toString())) {
				logger.error("Tried to register a Facade but there is already a Facade registered with the same mapping");
			}
			mappings.put(facadeBaseSb.toString(), facade);
		}
		setUrlMap(mappings);
	}

	public List<Facade> getMappedFacades() {
		return facades;
	}

}
