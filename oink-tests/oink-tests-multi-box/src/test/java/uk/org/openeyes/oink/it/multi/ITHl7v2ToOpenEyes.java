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
package uk.org.openeyes.oink.it.multi;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.rest.client.HttpBasicAuthInterceptor;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.IRestfulClientFactory;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ACK;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * 
 * Tests the behaviour of a remote OINK instance, receiving HL7v2 messages
 * and routing them to another OINK instance bound to OpenEyes
 * 
 * @author Oliver Wilkie
 */
public class ITHl7v2ToOpenEyes {
	
	private static Logger logger = LoggerFactory
			.getLogger(ITHl7v2ToOpenEyes.class);

	private static Properties properties;
	private static IGenericClient openeyesClient;
	
	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {
		properties = new Properties();
		InputStream is = ITHl7v2ToOpenEyes.class.getResourceAsStream("ITHl7v2ToOpenEyes.properties");
		properties.load(is);
		openeyesClient = buildOpenEyesClient(properties);
	}

	@Test
	public void testA01CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A01
		Message exampleA01 = loadHl7Message("/example-messages/hl7v2/A01-mod.txt");

		// Post A01
		testMessageCanBePostedAndAcceptedByOink(exampleA01);

		// Search for Patient
		Bundle searchResults = openeyesClient
				.search()
				.forResource(ca.uhn.fhir.model.dstu.resource.Patient.class)
				.where(ca.uhn.fhir.model.dstu.resource.Patient.IDENTIFIER
						.exactly().identifier("9999999999"))
				.and(ca.uhn.fhir.model.dstu.resource.Patient.GIVEN.matches()
						.value("Test")).execute();
		
		assertEquals(1, searchResults.getEntries().size());
	}

	@Test
	public void testA05CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A05
		Message exampleA05 = loadHl7Message("/example-messages/hl7v2/A05-mod.txt");

		// Post A01
		testMessageCanBePostedAndAcceptedByOink(exampleA05);

		// Search for Patient
		Bundle searchResults = openeyesClient
				.search()
				.forResource(ca.uhn.fhir.model.dstu.resource.Patient.class)
				.where(ca.uhn.fhir.model.dstu.resource.Patient.IDENTIFIER
						.exactly().identifier("9999999999"))
				.and(ca.uhn.fhir.model.dstu.resource.Patient.GIVEN.matches()
						.value("Testdon")).execute();

		assertEquals(1, searchResults.getEntries().size());

	}

	@Test
	public void testA28CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A28
		Message exampleA28 = loadHl7Message("/example-messages/hl7v2/A28-2-mod.txt");

		// Post A28
		testMessageCanBePostedAndAcceptedByOink(exampleA28);

		// Search for Patient
		Bundle searchResults = openeyesClient
				.search()
				.forResource(ca.uhn.fhir.model.dstu.resource.Patient.class)
				.where(ca.uhn.fhir.model.dstu.resource.Patient.IDENTIFIER
						.exactly().identifier("6509874369"))
				.and(ca.uhn.fhir.model.dstu.resource.Patient.GIVEN.matches()
						.value("RANDELL")).execute();

		assertEquals(1, searchResults.getEntries().size());

	}

	@Test
	public void testA31CreatePatientLeadsToANewPatientInEndServer()
			throws Exception {

		// Load example A31
		Message exampleA31 = loadHl7Message("/example-messages/hl7v2/A31-2-mod.txt");

		// Post A31
		testMessageCanBePostedAndAcceptedByOink(exampleA31);

		// Search for Patient
		Bundle searchResults = openeyesClient
				.search()
				.forResource(ca.uhn.fhir.model.dstu.resource.Patient.class)
				.where(ca.uhn.fhir.model.dstu.resource.Patient.IDENTIFIER
						.exactly().identifier("4148734654"))
				.and(ca.uhn.fhir.model.dstu.resource.Patient.GIVEN.matches()
						.value("RICHIE")).execute();

		assertEquals(1, searchResults.getEntries().size());

	}

	public void testMessageCanBePostedAndAcceptedByOink(Message m)
			throws Exception {
		// Send message and get ACK response
		HapiContext context = new DefaultHapiContext();
		
		
		Connection hl7v2Conn = context.newClient(
				(String) properties.get("hl7v2.host"),
				(Integer) Integer.parseInt(properties.getProperty("hl7v2.port")),
				false);
		Initiator initiator = hl7v2Conn.getInitiator();
		ACK response = (ACK) initiator.sendAndReceive(m);
		context.close();

		assertEquals("AA", response.getMSA().getAcknowledgementCode()
				.getValue());
	}
	
	public static IGenericClient buildOpenEyesClient(Properties props) {
		String proxyUri = (String) props.get("openeyes.uri");

		// Create a context and get the client factory so it can be configured
		FhirContext ctx = new FhirContext();
		IRestfulClientFactory clientFactory = ctx.getRestfulClientFactory();

		// Create an HTTP Client Builder
		HttpClientBuilder builder = HttpClientBuilder.create();

		// This interceptor adds HTTP username/password to every request
		String username = (String) props.get("openeyes.username");
		String password = (String) props.get("openeyes.password");
		builder.addInterceptorFirst(new HttpBasicAuthInterceptor(username,
				password));
		
		
		builder.addInterceptorFirst(new HttpRequestInterceptor() {
			
			@Override
			public void process(HttpRequest req, HttpContext context)
					throws HttpException, IOException {
				req.addHeader("Accept", "application/json+fhir; charset=UTF-8");				
			}
		});

		// Use the new HTTP client builder
		clientFactory.setHttpClient(builder.build());
		
		IGenericClient client = clientFactory.newGenericClient("http://"
				+ proxyUri);
		
		return client;
	}
	
	public static Message loadHl7Message(String path) throws IOException, HL7Exception {
		InputStream is = ITHl7v2ToOpenEyes.class.getResourceAsStream(path);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		String message = writer.toString();
		logger.info(message.replace("\r", "\n"));
		@SuppressWarnings("resource")
		HapiContext context = new DefaultHapiContext();
		context.setValidationContext(new NoValidation());
		Parser p = context.getPipeParser();
		Message adt = p.parse(message);
		return adt;
	}

}
