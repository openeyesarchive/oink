package uk.org.openeyes.oink.infrastructure;

import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;

import uk.org.openeyes.oink.domain.HttpMethod;

public class RequestMapperBeanDefinitionParser extends AbstractSingleBeanDefinitionParser  {

	public HttpMethod parseMethod(String method) {
		if(method == null) {
			return HttpMethod.ANY;
		} else {
			return HttpMethod.valueOf(method.toUpperCase());
		}
	}
}
