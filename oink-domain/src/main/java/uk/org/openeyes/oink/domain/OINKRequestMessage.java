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
import java.util.LinkedList;
import java.util.List;

import org.hl7.fhir.instance.model.AtomCategory;

import uk.org.openeyes.oink.domain.json.OinkRequestMessageJsonConverter;

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
	private List<AtomCategory> tags;
	
	private FhirBody body;

	public OINKRequestMessage() {
		tags = new LinkedList<AtomCategory>();
	}

	public OINKRequestMessage(String origin, String destination,
			String resourcePath, HttpMethod method, String query, FhirBody body) {
		this();
		this.origin = origin;
		this.destination = destination;
		this.resourcePath = resourcePath;
		this.method = method;
		this.body = body;
		this.query = query;
	}
	
	public void addProfile(String profileTerm) {
		AtomCategory cat = new AtomCategory("http://hl7.org/fhir/tag/profile", profileTerm, "");
		tags.add(cat);
	}
	
	public List<AtomCategory> getTags() {
		return tags;
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

	@Override
	public String toString() {
		return "OINKRequestMessage [resourcePath=" + resourcePath + ", method="
				+ method + ", query=" + query + ", body=" + body + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((query == null) ? 0 : query.hashCode());
		result = prime * result
				+ ((resourcePath == null) ? 0 : resourcePath.hashCode());
		return result;
	}

	/**
	 * Note. FHIR Implementation does not implement equals() so we compare JSON
	 * representations instead
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OINKRequestMessage other = (OINKRequestMessage) obj;
		OinkRequestMessageJsonConverter converter = new OinkRequestMessageJsonConverter();
		String thisJson = converter.toJsonString(this);
		String otherJson = converter.toJsonString(other);
		return thisJson.equals(otherJson);
	}

}
