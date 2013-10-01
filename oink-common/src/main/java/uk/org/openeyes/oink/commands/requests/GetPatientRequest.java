/*******************************************************************************
 * OpenEyes Interop Toolkit
 * Copyright (C) 2013  OpenEyes Foundation (http://www.openeyes.org.uk)
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
 ******************************************************************************/
package uk.org.openeyes.oink.commands.requests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GetPatientRequest {
	
	public GetPatientRequest() {
		
	}
	
	public GetPatientRequest(String identifier) {
		this.identifiers = new ArrayList<String>();
		this.identifiers.add(identifier);
	}

	public GetPatientRequest(Collection<String> identifiers) {
		this.identifiers = new ArrayList<String>();
		this.identifiers.addAll(identifiers);
	}

	public GetPatientRequest(String[] identifiers) {
		// Use Google Guava to do the hard work
		this.identifiers = Arrays.asList(identifiers);
	}

	private List<String> identifiers;

	public List<String> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<String> identifiers) {
		this.identifiers = identifiers;
	}
}
