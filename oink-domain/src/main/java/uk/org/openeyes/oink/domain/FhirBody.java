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

import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;

public class FhirBody {
	
	private final AtomFeed bundle;
	private final Resource r;
	
	public FhirBody(AtomFeed a) {
		this.bundle = a;
		this.r = null;
	}
	
	public FhirBody(Resource r) {
		this.bundle = null;
		this.r = r;
	}
	
	public boolean isBundle() {
		return bundle != null;
	}
	
	public boolean isResource() {
		return r != null;
	}
	
	public AtomFeed getBundle() {
		return bundle;
	}
	
	public Resource getResource() {
		return r;
	}

}
