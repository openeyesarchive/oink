/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.facade;

import java.util.regex.Pattern;

import org.springframework.http.HttpMethod;

/**
 * A 'key' instantiated with a FHIR resource and method. Resources are can be explicit or can end in a wildcard '*'
 * Keys are comparable. A more 'general' resource is considered greater than a more granular resource path. 
 * Similarly, if two keys have the same resource path but the second one is HTTPMethod.ANY the latter is considered to be higher.
 * @author Oliver Wilkie
 */
public class RabbitMapperKey implements Comparable<RabbitMapperKey> {
		
	String originalResource;
	String regexResource;
	HttpMethod method;	// If null, that means ANY method
		
	public RabbitMapperKey(String resource) {
		this(resource, null);
	}
	
	public RabbitMapperKey(String resource, HttpMethod method) {
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

	public HttpMethod getMethod() {
		return method;
	}
	
	public boolean matches(String realResourcePath, HttpMethod method) {
		// Check methods match
		boolean methodsMatch = this.method == null || method == this.method;
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
	public int compareTo(RabbitMapperKey o) {
		if (this.originalResource.equals(o.originalResource)) {
			if (this.method == null && o.getMethod() == null) {
				return 0;
			} else if (this.method == null) {
				return 1;
			} else if (o.getMethod() == null) {
				return -1;
			} else {
				return this.method.compareTo(o.getMethod());
			}
		} else {
			return this.originalResource.compareTo(o.originalResource)*-1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime
				* result
				+ ((originalResource == null) ? 0 : originalResource.hashCode());
		result = prime * result
				+ ((regexResource == null) ? 0 : regexResource.hashCode());
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
		RabbitMapperKey other = (RabbitMapperKey) obj;
		if (method != other.method)
			return false;
		if (originalResource == null) {
			if (other.originalResource != null)
				return false;
		} else if (!originalResource.equals(other.originalResource))
			return false;
		if (regexResource == null) {
			if (other.regexResource != null)
				return false;
		} else if (!regexResource.equals(other.regexResource))
			return false;
		return true;
	}
	
	

}
