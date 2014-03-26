/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package uk.org.openeyes.oink.messaging;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

import uk.org.openeyes.oink.domain.OINKRequestMessage;

public class TestOinkMessageConverter {
	

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testCanUnwrapAValidOINKRequestMessage() {
		// Build a message containing 
		OINKRequestMessage req = new OINKRequestMessage();
		MessageProperties props = new MessageProperties();
		Jackson2JsonMessageConverter conv = new Jackson2JsonMessageConverter();
		Message m = conv.toMessage(req, props);
		
		OinkMessageConverter myConv = new OinkMessageConverter();
		OINKRequestMessage unravelled = (OINKRequestMessage) myConv.fromMessage(m);
	}
	
	@Test
	public void testCanHandleNonOinkJson() {
		Jackson2JsonMessageConverter conv = new Jackson2JsonMessageConverter();
		String blah = "{name: 'Oliver Wilkie', postcode: 'E20DS'}";
		MessageProperties props = new MessageProperties();
		Message m = conv.toMessage(blah, props);
		props.getHeaders().remove("__TypeId__");
				
		OinkMessageConverter myConv = new OinkMessageConverter();
		InvalidOinkMessageException unravelled = (InvalidOinkMessageException) myConv.fromMessage(m);
	}
	
	@Test
	public void testCanHandeNonJson() {
		SimpleMessageConverter conv = new SimpleMessageConverter();
		byte[] randomBytes = new String("HISDOIJS").getBytes();
		MessageProperties props = new MessageProperties();
		Message m = conv.toMessage(randomBytes, props);
		
		OinkMessageConverter myConv = new OinkMessageConverter();
		InvalidOinkMessageException unravelled = (InvalidOinkMessageException) myConv.fromMessage(m);
		
	}

}
