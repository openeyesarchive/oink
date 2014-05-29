package uk.org.openeyes.oink.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import uk.org.openeyes.oink.domain.OINKResponseMessage;

public class TestOinkMessageConverter {

	@Test
	public void testCanParseResponseFromOink() throws IOException {
		
		InputStream is = getClass().getResourceAsStream("/oinkresponsemessages/searchResults.json");
		
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String theString = writer.toString();
				
		OinkMessageConverter conv = new OinkMessageConverter();
		
		OINKResponseMessage resp = conv.responseMessageFromJsonString(theString);
		
	}
	
}
