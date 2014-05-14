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
package uk.org.openeyes.oink.messaging;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.http.InvalidFhirRequestException;

public class OinkMessageValidator {
	
	public void validateRequest(OINKRequestMessage message) throws Exception {
		validateBody(message);
	}

	public void validateBody(OINKRequestMessage message) throws Exception {
		
		String verb = message.getMethod().toString();
		FhirBody body = message.getBody();
		
		// Check content-type
		if (verb.equals("PUT") || verb.equals("POST")) {
			if (body == null) {
				throw new InvalidFhirRequestException("Invalid Body. A body is required for the verb: "+verb);
			}
		} else {
			if (body != null) {
				throw new InvalidFhirRequestException("Body detected. A body is not applicable for the verb: "+verb);
			}
		}
	}

}
