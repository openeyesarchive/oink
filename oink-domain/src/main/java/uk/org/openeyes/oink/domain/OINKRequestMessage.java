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
package uk.org.openeyes.oink.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This message encapsulates a request for a resource (FHIR REST style)
 * somewhere on the OINK System.
 * 
 * @author Oliver Wilkie
 * 
 */
public class OINKRequestMessage extends OINKMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * OINK Headers
	 */
	private String origin;
	private String destination;
	
	/*
	 * FHIR REST components
	 */
	private String resourcePath; // URI path corresponding to a Fhir Resource
	private HttpMethod method; // HTTP Request Method e.g. GET, POST
	private String query;

	private FhirBody body;

	public OINKRequestMessage() {
	}

	public OINKRequestMessage(String origin, String destination, String resourcePath, HttpMethod method, String query, FhirBody body) {
		this.origin = origin;
		this.destination = destination;
		this.resourcePath = resourcePath;
		this.method = method;
		this.body = body;
		this.query = query;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public FhirBody getBody() {
		return body;
	}

	public boolean hasBody() {
		return body != null;
	}

	public void setBody(FhirBody body) {
		this.body = body;
	}

	public String getParameters() {
		return query;
	}

	public void setParameters(String query) {
		if (query != null) {
			this.query = query;
		}
	}

}
