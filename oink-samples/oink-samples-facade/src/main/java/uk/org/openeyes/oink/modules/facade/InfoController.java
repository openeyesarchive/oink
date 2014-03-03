package uk.org.openeyes.oink.modules.facade;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.Controller;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public class InfoController implements Controller {
	
	@Autowired
	SimpleUrlHandlerMapping mappingBean;

	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		List<String> resources = new LinkedList<String>();
		List<String> methods = new LinkedList<String>();
		// Get list of mappings
		Map<String, Object> mappings = mappingBean.getHandlerMap();
		for (Entry<String, Object> entry : mappings.entrySet()) {
			String facadeMapping = entry.getKey();
			if (facadeMapping.equals("/"))
				continue;
			facadeMapping = facadeMapping.replace("*", "");
			Facade facade = (Facade) entry.getValue();
			HttpMapper<RabbitRoute> matcher = facade.getMapper();
			for (Pair<String, HttpMethod> key : matcher.getHttpKey()) {
				String resource = key.getValue0();
				String method = key.getValue1() == null ? "ALL" : key.getValue1().toString();
				methods.add(method);
				resources.add(facadeMapping+resource);
			}
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("resources", resources);
		model.put("methods", methods);
		return new ModelAndView("welcome", model);
	}

}
