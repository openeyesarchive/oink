package uk.org.openeyes.oink.domain;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OINKRequestMessageTest {
	
	@Test
	public void testMessageIsSerialisableToJSONWithJackson() throws JsonGenerationException, JsonMappingException, IOException {
		HttpHeaders headers = new HttpHeaders();
		byte[] body = new byte[20];
		OINKRequestMessage message = new OINKRequestMessage("/path", HttpMethod.GET, headers, body);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(message);
		OINKRequestMessage deserialised = mapper.readValue(json, OINKRequestMessage.class);
		
		Assert.assertEquals(message, deserialised);
	}
	

}
