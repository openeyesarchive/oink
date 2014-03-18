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
