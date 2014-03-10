package uk.org.openeyes.oink.modules.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * Based on the {@link SimpleUrlHandlerMapping} bean, this class maps incoming
 * urls to facade controllers based on the facade controller's associated
 * service name. E.g. Silverlink
 * 
 * @author Oliver Wilkie
 */
public class SimpleFacadeHandlerMapping extends SimpleUrlHandlerMapping {

	private final List<Facade> facades;

	public SimpleFacadeHandlerMapping(List<Facade> facades) {
		super();
		this.facades = facades;
		Map<String, Object> mappings = new HashMap<String, Object>();
		for (Facade facade : facades) {
			mappings.put("/" + facade.getServiceName() + "/**", facade);
		}
		setUrlMap(mappings);
	}

	public List<Facade> getMappedFacades() {
		return facades;
	}

}
