package uk.org.openeyes.oink.infrastructure;


import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import uk.org.openeyes.oink.modules.facade.RabbitMapper;
import uk.org.openeyes.oink.modules.facade.RabbitMapper.RabbitMapping;

public class RabbitMapperBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	protected Class<?> getBeanClass(Element element) {
		return RabbitMapper.class;
	}
	
	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		
		List<Element> mappingElems = DomUtils.getChildElements(element);
		
		RabbitMapping[] mappings = new RabbitMapping[mappingElems.size()];
		for(int i = 0; i < mappingElems.size(); i++) {
			Element mappingElem = mappingElems.get(i);
			String route = mappingElem.getAttribute("route");
			String exchange = mappingElem.getAttribute("exchange");
			String resource = mappingElem.getAttribute("resource");
			String method = mappingElem.getAttribute("method");
			mappings[i] = new RabbitMapping(resource, method, route, exchange);
		}
		bean.addConstructorArgValue(mappings);

	}

}
