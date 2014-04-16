package uk.org.openeyes.oink.facade;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public interface RoutingService {
	
	public RabbitRoute getRouting(String path, HttpMethod method);

}
