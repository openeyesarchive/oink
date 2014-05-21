package uk.org.openeyes.oink.http;

import java.io.InputStream;

import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.junit.Test;

import uk.org.openeyes.oink.domain.FhirBody;

public class TestOinkHttpConverter {

	
	@Test
	public void testReadFhirBody() throws Exception {
		
		OinkHttpConverter conv = new OinkHttpConverter();
		
		InputStream is = TestOinkHttpConverter.class.getResourceAsStream("/fhir/searchResults.json");
		
		FhirBody body = conv.readFhirBody(is);
		
	}
	
}
