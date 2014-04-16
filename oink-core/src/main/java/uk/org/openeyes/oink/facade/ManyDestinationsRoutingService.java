package uk.org.openeyes.oink.facade;

import java.util.HashMap;
import java.util.Map;

import uk.org.openeyes.oink.common.HttpMapper;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;

public class ManyDestinationsRoutingService {
	
	private final Map<String, HttpMapper<RabbitRoute>> routeMaps;
	
	public ManyDestinationsRoutingService(Map<String, HttpMapper<RabbitRoute>> routeMaps) {
		this.routeMaps = routeMaps;
	}
	
	/**
	 * Initializes a Routing Service with same mappings for all destinations.
	 * @param routeMap
	 */
	public ManyDestinationsRoutingService(HttpMapper<RabbitRoute> routeMap) {
		routeMaps = new HashMap<>();
		routeMaps.put("*", routeMap);
	}

	public boolean isDestinationValid(String destination) {
		return (routeMaps.size() == 1 && routeMaps.containsKey("*")) || routeMaps.containsKey(destination);
	}

	public RabbitRoute getRouting(String destination, String fhirUri,
			HttpMethod method) {
		HttpMapper<RabbitRoute> destinationMapper;
		if (routeMaps.size() == 1 && routeMaps.containsKey("*")) {
			destinationMapper = routeMaps.get("*");
		} else {
			destinationMapper = routeMaps.get(destination); 
		}
		if (destinationMapper != null) {
			return destinationMapper.get(fhirUri, method);
		} else {
			return null;
		}
	}

}
