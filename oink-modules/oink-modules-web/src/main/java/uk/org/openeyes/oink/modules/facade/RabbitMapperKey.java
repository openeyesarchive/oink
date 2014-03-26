/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package uk.org.openeyes.oink.modules.facade;

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
