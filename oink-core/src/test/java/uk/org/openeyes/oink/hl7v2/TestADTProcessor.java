package uk.org.openeyes.oink.hl7v2;

import org.junit.Assert;
import org.junit.Test;

public class TestADTProcessor {
	
	@Test
	public void testExtractRelativeUrlFromLocation() {
		String location = "http://192.168.1.101:80/api/Practitioner/gp-4/_history/1401788329";
		
		String relative = ADTProcessor.extractResourceRelativeUrlFromLocation(location, "Practitioner");
		
		Assert.assertEquals("Practitioner/gp-4",relative);
	}

}
