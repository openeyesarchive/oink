package uk.org.openeyes.oink.modules.facade;

import uk.org.openeyes.oink.domain.HttpMethod;

public class NoRabbitMappingFoundException extends Exception {

	public NoRabbitMappingFoundException(String resource, HttpMethod method) {
		super(String.format("No rabbit exchange and routing key found for request operation %s "
				+ "on resource %s for system handled by this facade", resource, method.toString()));
	}
}
