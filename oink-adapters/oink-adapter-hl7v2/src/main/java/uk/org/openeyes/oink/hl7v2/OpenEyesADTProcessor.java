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
package uk.org.openeyes.oink.hl7v2;

import java.util.List;

import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Resource;

import uk.org.openeyes.oink.domain.OINKRequestMessage;

/**
 * An extension of {@link ADTProcessor} which adds OpenEyes metadata to messages
 * routing over Oink.
 * 
 * @author Oliver Wilkie
 */
public class OpenEyesADTProcessor extends ADTProcessor {

	private String gpProfile = "http://openeyes.org.uk/fhir/profile/Practitioner/Gp";
	private String practiceProfile = "http://openeyes.org.uk/fhir/profile/Organization/Practice";

	@Override
	protected OINKRequestMessage buildSearchRequestMessage(Resource resource,
			List<Identifier> ids) {
		OINKRequestMessage query = super.buildSearchRequestMessage(resource,
				ids);

		if (resource.getResourceType().toString().equals("Organization")) {
			query.setParameters(query.getParameters().concat(
					"&_profile=" + practiceProfile));
		} else if (resource.getResourceType().toString().equals("Practitioner")) {
			query.setParameters(query.getParameters().concat(
					"&_profile=" + gpProfile));
		}

		return query;
	}

	@Override
	protected OINKRequestMessage buildPostRequestMessage(Resource resource) {
		OINKRequestMessage query = super.buildPostRequestMessage(resource);

		if (resource.getResourceType().toString().equals("Organization")) {
			query.addProfile(practiceProfile);
		} else if (resource.getResourceType().toString().equals("Practitioner")) {
			query.addProfile(gpProfile);
		}

		return query;
	}

	public void setGpProfile(String gpProfile) {
		this.gpProfile = gpProfile;
	}

	public void setPracticeProfile(String practiceProfile) {
		this.practiceProfile = practiceProfile;
	}
}
