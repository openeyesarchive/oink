package uk.org.openeyes.oink.infrastructure;


import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.org.openeyes.oink.modules.facade.RabbitMapper;
import uk.org.openeyes.oink.modules.facade.RabbitMapper.RabbitMapping;

public class RabbitMapperBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	protected Class getBeanClass(Element element) {
		return RabbitMapper.class;
	}
	
	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		
		NodeList mappingElems = element.getChildNodes();
		RabbitMapping[] mappings = new RabbitMapping[mappingElems.getLength()];
		for(int i = 0; i < mappingElems.getLength(); i++) {
			Element mappingElem = (Element) mappingElems.item(i);
			String route = mappingElem.getAttribute("route");
			String exchange = mappingElem.getAttribute("exchange");
			String resource = mappingElem.getAttribute("resource");
			String method = mappingElem.getAttribute("method");
			mappings[i] = new RabbitMapping(resource, method, route, exchange);
		}
		bean.addConstructorArgValue(mappings);

	}

}
