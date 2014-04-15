package uk.org.openeyes.oink.modules.facade;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public class SingleDestinationRoutingService implements RoutingService {
	
	private final HttpMapper<RabbitRoute> mapper;
	private final String destination;
	
	public SingleDestinationRoutingService(String destination, HttpMapper<RabbitRoute> mapper) {
		this.destination = destination;
		this.mapper = mapper;
	}

	@Override
	public boolean isDestinationValid(String destination) {
		return destination.equals(this.destination);
	}

	@Override
	public RabbitRoute getRouting(String destination, String fhirUri,
			HttpMethod method) {
		if (!destination.equals(this.destination)) {
			return null;
		}
		return mapper.get(fhirUri, method);
	}

}
