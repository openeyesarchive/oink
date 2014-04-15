/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
