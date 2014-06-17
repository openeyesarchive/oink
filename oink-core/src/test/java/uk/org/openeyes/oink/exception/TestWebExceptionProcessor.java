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
package uk.org.openeyes.oink.exception;

import static org.junit.Assert.*;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestWebExceptionProcessor {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void testHandlesNoException() throws Exception {
		CamelContext c = new DefaultCamelContext();
		Exchange e = new DefaultExchange(c);
		e.setException(new OinkException() {
		});
		
		WebExceptionProcessor webProcessor = new WebExceptionProcessor();
		webProcessor.process(e);
		
		assertEquals(null, e.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE));		
	}

	@Test
	public void test() throws Exception {
		CamelContext c = new DefaultCamelContext();
		Exchange e = new DefaultExchange(c);
		e.setProperty(Exchange.EXCEPTION_CAUGHT, new OinkException() {
		});
		
		WebExceptionProcessor webProcessor = new WebExceptionProcessor();
		webProcessor.process(e);
		
		assertEquals(500, e.getOut().getHeader(Exchange.HTTP_RESPONSE_CODE));
	}

}
