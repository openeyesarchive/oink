package uk.org.openeyes.oink.modules.facade;

import java.util.LinkedList;
import java.util.List;

import org.springframework.http.HttpMethod;

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
		this.mappings = mappings.clone();
	}

	
	public RabbitRoute getMapping(String resource, HttpMethod method) {
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
			RabbitMapping e = new RabbitMapping(fhirResource,fhirMethod, rabbitKey, rabbitExchange);
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
		
		public RabbitMapping(String fhirResource, String fhirMethod, String rabbitKey, String rabbitExchange) {
			this.request = new RabbitMapperKey(fhirResource, HttpMethod.valueOf(fhirMethod));
			this.route = new RabbitRoute(rabbitKey, rabbitExchange);
		}
		
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((request == null) ? 0 : request.hashCode());
			result = prime * result + ((route == null) ? 0 : route.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RabbitMapping other = (RabbitMapping) obj;
			if (request == null) {
				if (other.request != null)
					return false;
			} else if (!request.equals(other.request))
				return false;
			if (route == null) {
				if (other.route != null)
					return false;
			} else if (!route.equals(other.route))
				return false;
			return true;
		}

	}

}
