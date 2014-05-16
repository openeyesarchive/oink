package uk.org.openeyes.oink.hl7v2;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.exception.OinkException;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

public class TestA19Builder extends Hl7TestSupport {

	@Test
	public void testBuildsSearchByNHSNumberQueryOk() throws HL7Exception, IOException,
			OinkException {

		// Build incoming request
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Patient");
		req.setMethod(HttpMethod.GET);
		req.setDestination(null);
		req.setBody(null);
		req.setParameters("foo=bar&identifier=NHS|1234567890&foo2=bar2");

		// Build Hl7v2 query
		A19Processor builder = new A19Processor();
		Message generatedMessage = builder.buildQuery(req);

		// Load existing Hl7v2 message
		Message existingMessage = loadMessage("/hl7v2/QRYA19-nhsnumber-1.txt");
		// Update timestamps and ids in sample message
		Terser existingTerser = new Terser(existingMessage);
		Terser generatedTerser = new Terser(generatedMessage);
		existingTerser.set("/MSH-10", generatedTerser.get("/MSH-10"));
		existingTerser.set("/MSH-7-1", generatedTerser.get("/MSH-7-1"));
		existingTerser.set("/QRD-1-1", generatedTerser.get("/QRD-1-1"));
		existingTerser.set("/QRD-4", generatedTerser.get("/QRD-4"));

		// Ensure match
		assertEquals(existingMessage.toString(), generatedMessage.toString());
	}
	
	@Ignore
	@Test
	public void buildA19Response() throws HL7Exception, IOException {

	}
	
	@Test
	public void testGeneratesQryMessageOkForSearchBySurname() throws HL7Exception, IOException,
			OinkException {

		// Build incoming request
		OINKRequestMessage req = new OINKRequestMessage();
		req.setResourcePath("/Patient");
		req.setMethod(HttpMethod.GET);
		req.setDestination(null);
		req.setBody(null);
		req.setParameters("foo=bar&family=WILKIE&foo2=bar2");

		// Build Hl7v2 query
		A19Processor builder = new A19Processor();
		Message generatedMessage = builder.buildQuery(req);

		// Load existing Hl7v2 message
		Message existingMessage = loadMessage("/hl7v2/QRYA19-familyname-1.txt");
		// Update timestapms and ids in sample message
		Terser existingTerser = new Terser(existingMessage);
		Terser generatedTerser = new Terser(generatedMessage);
		existingTerser.set("/MSH-10", generatedTerser.get("/MSH-10"));
		existingTerser.set("/MSH-7-1", generatedTerser.get("/MSH-7-1"));
		existingTerser.set("/QRD-1-1", generatedTerser.get("/QRD-1-1"));
		existingTerser.set("/QRD-4", generatedTerser.get("/QRD-4"));

		// Ensure match
		assertEquals(existingMessage.toString(), generatedMessage.toString());
	}

}
