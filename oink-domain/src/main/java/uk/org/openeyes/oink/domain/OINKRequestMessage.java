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

import java.util.HashMap;
import java.util.Map;

/**
 * This message encapsulates a request for a resource (FHIR REST style)
 * somewhere on the OINK System.
 * 
 * @author Oliver Wilkie
 * 
 */
public class OINKRequestMessage extends OINKMessage {

	private String origin;
	private String destination;
	private String fhirResourcePath; // Portion of the original FHIR URL
										// following [base]. No leading slash.
	private HttpMethod method; // HTTP Request Method e.g. GET, POST
	private Map<String, String[]> parameters; // HTTP Query Parameters

	private OINKBody body;

	public OINKRequestMessage() {
		this.parameters = new HashMap<String, String[]>();
	}

	public OINKRequestMessage(String origin, String destination, String resourcePath, HttpMethod method, Map<String, String[]> params, OINKBody body) {
		this.origin = origin;
		this.destination = destination;
		this.fhirResourcePath = resourcePath;
		this.method = method;
		this.body = body;
		this.parameters = params;
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
		return fhirResourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.fhirResourcePath = resourcePath;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public OINKBody getBody() {
		return body;
	}

	public boolean hasBody() {
		return body != null;
	}

	public void setBody(OINKBody body) {
		this.body = body;
	}

	public Map<String, String[]> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String[]> parameters) {
		if (parameters != null) {
			this.parameters = parameters;
		}
	}

}
