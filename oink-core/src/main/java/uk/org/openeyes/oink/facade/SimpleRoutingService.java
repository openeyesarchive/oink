package uk.org.openeyes.oink.facade;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public class SimpleRoutingService implements RoutingService {
	
	private HttpMapper<RabbitRoute> mappings;

	@Override
	public RabbitRoute getRouting(String path, HttpMethod method) {
		return mappings.get(path, method);
	}
	
	public void setMappings(HttpMapper<RabbitRoute> mappings) {
		this.mappings = mappings;
	}

}
