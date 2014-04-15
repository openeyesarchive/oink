package uk.org.openeyes.oink.modules.facade;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public interface RoutingService {
	
	public boolean isDestinationValid(String destination);
	
	public RabbitRoute getRouting(String destination, String fhirUri, HttpMethod method);

}
