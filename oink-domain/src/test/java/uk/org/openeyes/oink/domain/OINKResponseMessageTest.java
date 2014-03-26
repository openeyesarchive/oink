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
