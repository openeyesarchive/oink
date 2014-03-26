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

import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceFactory;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OINKRequestMessageTest {
	
	@Test
	public void testMessageIsSerialisableToJSONWithJackson() throws Exception {
		Resource r = ResourceFactory.createResource("Patient"); // dummy resource
		OINKBody body = new OINKBody(r);
		OINKRequestMessage message = new OINKRequestMessage("service","/path", "GET", new HashMap<String, String>(), body);
		
		// Serialize
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(message);
		
		// Deserialize
		OINKRequestMessage deserialised = mapper.readValue(json, OINKRequestMessage.class);
		
		// Check the same
		Assert.assertTrue(messagesAreEqual(message, deserialised));
	}
	
	private boolean messagesAreEqual(OINKRequestMessage expected, OINKRequestMessage received) {
		OINKBody expectedBody = expected.getBody();
		OINKBody receivedBody = received.getBody();
		if (expectedBody.getFeed() != null) {
			if (!feedsAreEqual(expectedBody.getFeed(),receivedBody.getFeed())) {
				return false;
			}
		} else if (expectedBody.getResource() != null) {
			if (!resourcesAreEqual(expectedBody.getResource(), receivedBody.getResource())) {
				return false;
			}
		}
		if (!expected.getMethod().equals(received.getMethod())) {
			return false;
		}
		if (!expected.getResourcePath().equals(received.getResourcePath())) {
			return false;
		}
		if (!expected.getParameters().equals(received.getParameters())) {
			return false;
		}
		
		return true;
	}
	
	private boolean feedsAreEqual(AtomFeed expected, AtomFeed received) {
		return expected.getId().equals(received.getId());
	}
	
	private boolean resourcesAreEqual(Resource expected, Resource received) {
		return expected.getResourceType().equals(received.getResourceType());
	}
	

}
