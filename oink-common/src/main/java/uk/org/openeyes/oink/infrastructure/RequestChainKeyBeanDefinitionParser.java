package uk.org.openeyes.oink.infrastructure;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Triplet;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;

public class RequestChainKeyBeanDefinitionParser extends RequestMapperBeanDefinitionParser {

	protected Class<?> getBeanClass(Element element) {
		return HttpMapper.class;
	}
	
	protected void doParse(Element element, BeanDefinitionBuilder bean) {
		
		List<Element> mappingElems = DomUtils.getChildElements(element);
		
		List<Triplet<String,HttpMethod,String>> list = new ArrayList<Triplet<String,HttpMethod,String>>();
		
		for(int i = 0; i < mappingElems.size(); i++) {
			Element mappingElem = mappingElems.get(i);
			String resourcePath = mappingElem.getAttribute("resourcePath");
			HttpMethod method = parseMethod(mappingElem.getAttribute("method"));
			String resource = mappingElem.getAttribute("chainKey");
			list.add(new Triplet<String, HttpMethod, String>(resourcePath, method, resource));
		}
		
		bean.addConstructorArgValue(list);
	}
	
}
