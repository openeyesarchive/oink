package uk.org.openeyes.oink.domain;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OINKResponseMessageTest {
	
	@Test
	public void testMessageIsSerialisableToJSONWithJackson() throws JsonGenerationException, JsonMappingException, IOException {
		HttpHeaders headers = new HttpHeaders();
		byte[] body = new byte[20];
		OINKResponseMessage message = new OINKResponseMessage(HttpStatus.ACCEPTED, headers, body);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(message);
		OINKResponseMessage deserialised = mapper.readValue(json, OINKResponseMessage.class);
		
		Assert.assertEquals(message, deserialised);
	}
	

}
