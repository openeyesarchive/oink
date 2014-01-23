package uk.org.openeyes.oink.modules.facade;

import java.util.regex.Pattern;

import uk.org.openeyes.oink.domain.HTTPMethod;

/**
 * A 'key' instantiated with a FHIR resource and method. Resources are can be explicit or can end in a wildcard '*'
 * Keys are comparable. A more 'general' resource is considered greater than a more granular resource path. 
 * Similarly, if two keys have the same resource path but the second one is HTTPMethod.ANY the latter is considered to be higher.
 * @author Oliver Wilkie
 */
public class FhirRabbitMapperKey implements Comparable<FhirRabbitMapperKey> {
		
	String originalResource;
	String regexResource;
	HTTPMethod method;
		
	public FhirRabbitMapperKey(String resource) {
		this(resource, HTTPMethod.ANY);
	}
	
	public FhirRabbitMapperKey(String resource, HTTPMethod method) {
		originalResource = resource;
		setResource(resource);
		this.method = method;
	}
	
	/**
	 * Builds the resource as a regular expression which we can match real resource paths against
	 * @param resource a fhir resource path (optionally ending in a * wildcard)
	 */
	private void setResource(String resource) {
		if (resource.endsWith("*")) {
			// Handle wildcard
			String prefix = resource.substring(0, resource.length()-1);
			this.regexResource = Pattern.quote(prefix) + "(.)*";
		} else {
			// Treat at literal
			this.regexResource = Pattern.quote(resource);
		}
	}

	public String getResource() {
		return regexResource;
	}

	public HTTPMethod getMethod() {
		return method;
	}
	
	public boolean matches(String realResourcePath, HTTPMethod method) {
		// Check methods match
		boolean methodsMatch = method == this.method || this.method == HTTPMethod.ANY;
		if (methodsMatch) {
			// Check resource path matches
			return Pattern.matches(regexResource, realResourcePath);
		} else {
			return false;
		}
	}

	/**
	 * A more general match should be higher than a more granular match
	 */
	@Override
	public int compareTo(FhirRabbitMapperKey o) {
		if (this.originalResource.equals(o.originalResource)) {
			return this.method.compareTo(o.getMethod());
		} else {
			return this.originalResource.compareTo(o.originalResource)*-1;
		}
	}

}
