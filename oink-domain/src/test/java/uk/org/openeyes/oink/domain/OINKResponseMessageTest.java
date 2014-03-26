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

import java.io.IOException;
import java.util.List;

import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceFactory;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpStatusCodes;

public class OINKResponseMessageTest {
	
	@Test
	public void testMessageIsSerialisableToJSONWithJackson() throws JsonGenerationException, JsonMappingException, IOException {
		OINKBody body = new OINKBody();
		OINKResponseMessage message = new OINKResponseMessage(HttpStatusCodes.STATUS_CODE_OK, body);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(message);
		OINKResponseMessage deserialised = mapper.readValue(json, OINKResponseMessage.class);
	}
	
	@Test
	public void testFeedResponseMessageIsSerialisableAndDeserialisableToJSONWithJackson() throws Exception {
		// Prepare Non-empty Feed
		AtomFeed feed = new AtomFeed();
		feed.setTitle("FeedTitle");
		feed.setId("FeedId");
		List<AtomEntry<? extends Resource>> list = feed.getEntryList();
		AtomEntry<Patient> p = new AtomEntry<Patient>();
		p.setTitle("PatientTitle");
		p.setId("PatientId");
		Patient patient = (Patient) ResourceFactory.createResource("Patient");
		p.setResource(patient);
		list.add(p);
		
		OINKBody body = new OINKBody(feed);

		OINKResponseMessage message = new OINKResponseMessage(HttpStatusCodes.STATUS_CODE_OK, body);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(message);
		OINKResponseMessage deserialised = mapper.readValue(json, OINKResponseMessage.class);
	}
	
//	@Test
//	public void testCanHandleNullFields() throws JsonParseException, JsonMappingException, IOException {
//		ObjectMapper mapper = new ObjectMapper();
//		String json = "{\"status\":200,\"body\":{\"resource\":null,\"feed\":{\"feed\":{\"resourceType\":\"Bundle\",\"title\":null,\"id\":null}}}}";
//		OINKResponseMessage deserialised = mapper.readValue(json, OINKResponseMessage.class);
//	}
//	
	@Test
	public void testResourceResponseMessageIsSerialisableAndDeserialisableToJSONWithJackson() throws Exception {
		Resource patient = ResourceFactory.createResource("Patient");
		OINKBody body = new OINKBody(patient);

		OINKResponseMessage message = new OINKResponseMessage(HttpStatusCodes.STATUS_CODE_OK, body);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(message);
		OINKResponseMessage deserialised = mapper.readValue(json, OINKResponseMessage.class);
	}
	

}
