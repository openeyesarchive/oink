package uk.org.openeyes.oink.hl7v2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Address.AddressUse;
import org.hl7.fhir.instance.model.Contact.ContactUse;
import org.hl7.fhir.instance.model.Resource;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.exception.OinkException;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

public class TestA19Processor extends Hl7TestSupport {
	
	@Test
	public void testProcessorHandlesValidA19ResponseMessage() throws Exception {
		A19Processor processor = new A19Processor();
		processor.setXsltPath("/uk/org/openeyes/oink/hl7v2/a28ANDa31.xsl");
		testProcessorProducesExpectedOutput(processor, "/hl7v2/ADR-A19.txt", "/oinkrequestmessages/ADR-A19.json");		
	}
	
	@Ignore
	@Test
	public void buildExpectedResponseMessage() throws HL7Exception, IOException, ParseException {
				
		Patient p = new Patient();
		Identifier id = p.addIdentifier();
		id.setValueSimple("7111111");
		id.setSystemSimple("HISID");
		HumanName name = p.addName();
		name.addFamilySimple("Test");
		name.addGivenSimple("Test ");
		name.addPrefixSimple("Mr");
		p.setBirthDateSimple(new DateAndTime("1970-01-01"));
		CodeableConcept conc = new CodeableConcept();
		conc.addCoding().setCodeSimple("F").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Female");
		p.setGender(conc);
		
		Address add = p.addAddress();
		add.addLineSimple("Test Cottage");
		add.addLineSimple("Test Road, Test");
		add.setCitySimple("TESTBURY");
		add.setStateSimple("Testshire");
		add.setZipSimple("SP0 0BW");
		add.setUseSimple(AddressUse.home);		
		
		p.addTelecom().setUseSimple(ContactUse.home).setValueSimple("0999 999999");

		CodeableConcept mConc = new CodeableConcept();
		mConc.addCoding().setCodeSimple("M").setSystemSimple("http://hl7.org/fhir/vs/marital-status");
		p.setMaritalStatus(mConc);
		
		AtomFeed bundle = new AtomFeed();
		AtomEntry<Resource> entry = new AtomEntry<Resource>();
		entry.setResource(p);
		bundle.getEntryList().add(entry);
		
		FhirBody body = new FhirBody(bundle);
		OINKResponseMessage resp = new OINKResponseMessage(200);
		resp.setBody(body);
		
		OinkMessageConverter conv = new OinkMessageConverter();
		String s = conv.toJsonString(resp);
		Files.write(Paths.get("/Users/Oli/A19.json"), s.getBytes());
		
	}

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
