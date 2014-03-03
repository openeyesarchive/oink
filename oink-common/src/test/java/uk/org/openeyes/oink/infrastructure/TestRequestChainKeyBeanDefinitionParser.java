package uk.org.openeyes.oink.infrastructure;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.http.HttpMethod;

public class TestRequestChainKeyBeanDefinitionParser {

	@Test
	public void testCanParseHttpMethodRegardlessOfCase() {
		RequestMapperBeanDefinitionParser parser = new RequestMapperBeanDefinitionParser();
		assertEquals(HttpMethod.GET, parser.parseMethod("GET"));
		assertEquals(HttpMethod.GET, parser.parseMethod("get"));
	}

}
