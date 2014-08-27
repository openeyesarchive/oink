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
package uk.org.openeyes.oink.hl7v2.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.rabbitmq.RabbitMQEndpoint;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.fhir.ResourceConverter;
import uk.org.openeyes.oink.hl7v2.ADTProcessor;
import uk.org.openeyes.oink.hl7v2.ProcessorContext;
import ca.uhn.hl7v2.model.Message;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class TestHl7v2Messages extends Hl7TestSupport {

	private static final Logger log = LoggerFactory.getLogger(TestHl7v2Messages.class);
	
	@EndpointInject(uri="rabbitmq:5672/oink")
	private RabbitMQEndpoint endpoint;

	@BeforeClass
	public static void before() throws IOException {
		Properties props = new Properties();
		InputStream is = TestHl7v2ToRabbitRoute.class.getResourceAsStream("/hl7v2-test.properties");
		props.load(is);
	}
	
	@Before
	public void setUp() throws IOException {
		setProperties("/hl7v2-test.properties");
	}
	
	@Qualifier("a01Processor")
	@Autowired
	ADTProcessor a01Processor;

	@Test
	public void testIdentifierRemap() throws Exception {
		Patient patient = (Patient)processResource(a01Processor, "/example-messages/hl7v2/A01.txt");
		
		String fullName = patient.getName().get(0).getGiven().get(0).getValue() + " "
				+ patient.getName().get(0).getFamily().get(0).getValue();
		
		log.info("Patient = '{}'", fullName);
				
		endpoint.getCamelContext().stop();
		
		Assert.assertNotNull(patient);
		Assert.assertEquals("Joe Bloggs", fullName);
	}
	
	protected OINKResponseMessage process(ADTProcessor processor, String testMessageResourceFile) throws Exception {
		
		// Choose a message to send
		Message m = Hl7TestUtils.loadHl7Message(testMessageResourceFile);
		
		processor.setResolveCareProvider(false);
		processor.setResolveManagingOrganization(false);
		
		ProcessorContext processorContext = new ProcessorContext();
		processor.doProcess(m, null, processorContext);
		
		OINKResponseMessage response = null;
		
		for(Object o : processorContext.getContextHistory()) {
			if(o instanceof OINKRequestMessage) {
				OINKRequestMessage req = (OINKRequestMessage)o;
				
				response = new OINKResponseMessage();
				response.setBody(req.getBody());
				
				log.debug("Body ====>\n{}\n<====", ResourceConverter.toJsonString(response.getBody().getResource()));

				break;
			}
		}
		
		return response;
	}
	
	protected Resource processResource(ADTProcessor processor, String testMessageResourceFile) throws Exception {
		OINKResponseMessage r = process(processor, testMessageResourceFile);
		return r.getBody().getResource();
	}
}
