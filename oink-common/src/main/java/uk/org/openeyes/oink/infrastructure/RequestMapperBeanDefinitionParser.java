package uk.org.openeyes.oink.infrastructure;

import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.http.HttpMethod;

public class RequestMapperBeanDefinitionParser extends AbstractSingleBeanDefinitionParser  {

	public HttpMethod parseMethod(String method) {
		if(method == null) {
			return null;
		} else {
			return HttpMethod.valueOf(method.toUpperCase());
		}
	}
}
