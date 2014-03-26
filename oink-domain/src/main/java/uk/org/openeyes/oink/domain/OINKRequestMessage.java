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
