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
 * Can only be constructed using a {@link FhirRabbitMapperBuilder} 
 * and cannot be modified once constructed (thread-safe).
 * 
 * @author Oliver Wilkie
 * 
 */
public class FhirRabbitMapper {

	private final FhirRabbitMapping[] mappings;

	public FhirRabbitMapper(FhirRabbitMapperBuilder builder) {
		List<FhirRabbitMapping> list = builder.getMappings();
		mappings = new FhirRabbitMapping[list.size()];
		list.toArray(mappings);
	}
	
	public FhirRabbitMapper(FhirRabbitMapping[] mappings) {
		this.mappings = mappings;
	}

	
	public RabbitRoute getMapping(String resource, HTTPMethod method) {
		RabbitRoute result = null;
		FhirRabbitMapperKey finestMatch = null;
		for (FhirRabbitMapping e : mappings) {
			FhirRabbitMapperKey request = e.getRequest();
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
	public static class FhirRabbitMapperBuilder {
		
		private final List<FhirRabbitMapping> elems;
		
		public FhirRabbitMapperBuilder() {
			elems = new LinkedList<FhirRabbitMapper.FhirRabbitMapping>();
		}
		
		/**
		 * 
		 * @param fhirResource a resource path e.g. /Patient 
		 * @param fhirMethod an HTTP method - get,put,post,delete or blank for all.
		 * @param rabbitKey a RabbitMQ routing key
		 * @param rabbitExchange an optional exchange on the RabbitMQ server (blank for default)
		 */
		public void addMapping(String fhirResource, String fhirMethod, String rabbitKey, String rabbitExchange) {
			FhirRabbitMapperKey key = new FhirRabbitMapperKey(fhirResource, HTTPMethod.fromString(fhirMethod));
			RabbitRoute route = new RabbitRoute(rabbitKey, rabbitExchange);
			FhirRabbitMapping e = new FhirRabbitMapping(key,route);
			elems.add(e);
		}
		
		public List<FhirRabbitMapping> getMappings() {
			return elems;
		}
		
		public FhirRabbitMapper build() {
			return new FhirRabbitMapper(this);
		}
	}

	public static class FhirRabbitMapping implements Comparable<FhirRabbitMapping> {

		private final FhirRabbitMapperKey request;
		private final RabbitRoute route;
		
		public FhirRabbitMapping(FhirRabbitMapperKey request, RabbitRoute route) {
			this.request = request;
			this.route = route;
		}

		public FhirRabbitMapperKey getRequest() {
			return request;
		}
		
		public RabbitRoute getRoute() {
			return route;
		}

		@Override
		public int compareTo(FhirRabbitMapping o) {
			return request.compareTo(o.getRequest());
		}

	}

}
