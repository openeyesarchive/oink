package uk.org.openeyes.oink.modules.facade;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class ServiceFacadeTest {

	
	@Test
	public void testRegexMethod() {
		Pattern p = Pattern.compile("[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");  

	    Matcher m = p.matcher("/Patient"); 
		
		Assert.assertTrue(m.matches());
	}
	
}
