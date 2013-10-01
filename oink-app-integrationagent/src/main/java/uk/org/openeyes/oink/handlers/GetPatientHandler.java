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
package uk.org.openeyes.oink.handlers;

import java.util.ArrayList;
import java.util.List;

import uk.org.openeyes.oink.commands.requests.GetPatientRequest;
import uk.org.openeyes.oink.commands.response.GetPatientResponse;
import uk.org.openeyes.oink.domain.Patient;
import uk.org.openeyes.oink.infrastructure.commands.handler.CommandHandler;

public class GetPatientHandler implements CommandHandler<GetPatientRequest, GetPatientResponse> {

	@Override
	public GetPatientResponse handle(GetPatientRequest command) {
		
		List<Patient> patients = new ArrayList<Patient>();
		patients.add(new Patient("11111-1", "Tyrion", "Lannister"));
		patients.add(new Patient("11111-2", "Daenerys", "Targaryen"));
		patients.add(new Patient("11111-3", "Bran", "Stark"));
		return new GetPatientResponse(patients);
	}

}
