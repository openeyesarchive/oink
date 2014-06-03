package uk.org.openeyes.oink.fhir;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.AtomFeed;
import org.junit.Assert;
import org.junit.Test;

public class TestFhirConverter {
	
	@Test
	public void testCanParsePractionerSearchResultsFromOpenEyes() throws Exception {
		
		BundleParser conv = new BundleParser();
		InputStream is = getClass().getResourceAsStream("/example-messages/fhir/practitionerSearchResults.json");
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String message = writer.toString();
		AtomFeed result = conv.fromJsonOrXml(message);
		Assert.assertNotNull(result);
	}

}
