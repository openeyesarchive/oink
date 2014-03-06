package uk.org.openeyes.oink.modules.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

public class SimpleFacadeHandlerMapping extends SimpleUrlHandlerMapping {
	
	private final List<Facade> facades;
	
	public SimpleFacadeHandlerMapping(List<Facade> facades) {
		super();
		this.facades = facades;
		Map<String, Object> mappings = new HashMap<String, Object>();
		for (Facade facade: facades) {
			mappings.put("/"+facade.getServiceName()+"/**", facade);
		}
		setUrlMap(mappings);
	}
	
	public List<Facade> getMappedFacades() {
		return facades;
	}

}
