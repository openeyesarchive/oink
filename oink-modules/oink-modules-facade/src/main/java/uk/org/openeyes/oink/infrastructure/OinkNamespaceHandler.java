package uk.org.openeyes.oink.infrastructure;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class OinkNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("rabbitMapper", new RabbitMapperBeanDefinitionParser());
	}

}
