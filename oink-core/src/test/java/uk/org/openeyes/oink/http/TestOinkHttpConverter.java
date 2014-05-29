package uk.org.openeyes.oink.http;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.junit.Test;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

public class TestOinkHttpConverter {

	@Test
	public void testReadFhirBody() throws Exception {

		OinkHttpConverter conv = new OinkHttpConverter();

		InputStream is = TestOinkHttpConverter.class
				.getResourceAsStream("/example-messages/fhir/searchResults.json");

		FhirBody body = conv.readFhirBody(is);

	}

	@Test
	public void testCanHandleEmptySearchResults() throws Exception {
		OinkHttpConverter conv = new OinkHttpConverter();

		InputStream inputStream = this.getClass().getResourceAsStream(
				"/example-messages/fhir/noSearchResults.json");
		assertNotNull(inputStream);

		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer);
		String theString = writer.toString();

		OINKResponseMessage resp = conv.buildOinkResponse(
				new HashMap<String, Object>(), theString);
		assertNotNull(resp.getBody());
		assertNotNull(resp.getBody().getBundle());

	}

}
