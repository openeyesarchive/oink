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

	private String destination;
	private String fhirResourcePath; // Portion of the original FHIR URL
										// following [base]. No leading slash.
	private String method; // HTTP Request Method e.g. GET, POST
	private Map<String, String> parameters; // HTTP Query Parameters

	private OINKBody body;

	public OINKRequestMessage() {
		this.parameters = new HashMap<String, String>();
	}

	public OINKRequestMessage(String destination, String resourcePath, String method, Map<String, String> params, OINKBody body) {
		this.destination = destination;
		this.fhirResourcePath = resourcePath;
		this.method = method;
		this.body = body;
		this.parameters = params;
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

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
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

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		if (parameters != null) {
			this.parameters = parameters;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + body.hashCode();
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime
				* result
				+ ((fhirResourcePath == null) ? 0 : fhirResourcePath.hashCode());
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
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
		OINKRequestMessage other = (OINKRequestMessage) obj;
		if (!body.equals(other.body))
			return false;
		if (method != other.method)
			return false;
		if (fhirResourcePath == null) {
			if (other.fhirResourcePath != null)
				return false;
		} else if (!fhirResourcePath.equals(other.fhirResourcePath))
			return false;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination)) {
			return false;
		}
		return true;
	}

}
