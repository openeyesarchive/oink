package uk.org.openeyes.oink.infrastructure;


import java.util.ArrayList;
import java.util.List;

import org.javatuples.Triplet;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public class RequestRabbitMapperBeanDefinitionParser extends RequestMapperBeanDefinitionParser {

	protected Class<?> getBeanClass(Element element) {
		return HttpMapper.class;
	}
	
	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		
		List<Element> mappingElems = DomUtils.getChildElements(element);
		
		List<Triplet<String,HttpMethod,RabbitRoute>> list = new ArrayList<Triplet<String,HttpMethod,RabbitRoute>>();
		
		for(int i = 0; i < mappingElems.size(); i++) {
			Element mappingElem = mappingElems.get(i);
			String resource = mappingElem.getAttribute("resource");
			HttpMethod method = parseMethod(mappingElem.getAttribute("method"));
			RabbitRoute route = new RabbitRoute(mappingElem.getAttribute("route"), mappingElem.getAttribute("exchange"));
			list.add(new Triplet<String, HttpMethod, RabbitRoute>(resource, method, route));
		}
		bean.addConstructorArgValue(list);

	}
	
}
