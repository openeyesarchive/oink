package uk.org.openeyes.oink.modules.facade;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import uk.org.openeyes.oink.domain.HTTPMethod;
import uk.org.openeyes.oink.messaging.RabbitRoute;

/**
 * Simple Fhir resource to Rabbit Queuen name mapper.
 * Supports direct matches (given "/test" -> registered "/test") and "*" matches
 * (given "/test" -> registered "/t*"). Will always match against the most
 * specific resource.
 * 
 * Can only be constructed using a {@link RabbitMapperBuilder} 
 * and cannot be modified once constructed (thread-safe).
 * 
 * @author Oliver Wilkie#
 * 
 */
public class RabbitMapper {

	private final RabbitMapping[] mappings;

	public RabbitMapper(RabbitMapperBuilder builder) {
		List<RabbitMapping> list = builder.getMappings();
		mappings = new RabbitMapping[list.size()];
		list.toArray(mappings);
	}
	
	public RabbitMapper(RabbitMapping[] mappings) {
		this.mappings = mappings;
	}

	
	public RabbitRoute getMapping(String resource, HTTPMethod method) {
		RabbitRoute result = null;
		RabbitMapperKey finestMatch = null;
		for (RabbitMapping e : mappings) {
			RabbitMapperKey request = e.getRequest();
			if (request.matches(resource, method)) {
				if (finestMatch == null || request.compareTo(finestMatch) < 0) {
					finestMatch = request;
					result = e.getRoute();
				}
			}
		}
		return result;
	}
	
	/**
	 * Builder for a FhirRabbitMapper. Mappings are added using the addMapping method.
	 * 
	 * @author Oliver Wilkie
	 */
	public static class RabbitMapperBuilder {
		
		private final List<RabbitMapping> elems;
		
		public RabbitMapperBuilder() {
			elems = new LinkedList<RabbitMapper.RabbitMapping>();
		}
		
		/**
		 * 
		 * @param fhirResource a resource path e.g. /Patient 
		 * @param fhirMethod an HTTP method - get,put,post,delete or blank for all.
		 * @param rabbitKey a RabbitMQ routing key
		 * @param rabbitExchange an optional exchange on the RabbitMQ server (blank for default)
		 */
		public void addMapping(String fhirResource, String fhirMethod, String rabbitKey, String rabbitExchange) {
			RabbitMapperKey key = new RabbitMapperKey(fhirResource, HTTPMethod.fromString(fhirMethod));
			RabbitRoute route = new RabbitRoute(rabbitKey, rabbitExchange);
			RabbitMapping e = new RabbitMapping(key,route);
			elems.add(e);
		}
		
		public List<RabbitMapping> getMappings() {
			return elems;
		}
		
		public RabbitMapper build() {
			return new RabbitMapper(this);
		}
	}

	public static class RabbitMapping implements Comparable<RabbitMapping> {

		private final RabbitMapperKey request;
		private final RabbitRoute route;
		
		public RabbitMapping(RabbitMapperKey request, RabbitRoute route) {
			this.request = request;
			this.route = route;
		}

		public RabbitMapperKey getRequest() {
			return request;
		}
		
		public RabbitRoute getRoute() {
			return route;
		}

		@Override
		public int compareTo(RabbitMapping o) {
			return request.compareTo(o.getRequest());
		}

	}

}
