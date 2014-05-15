package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Boolean;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Address.AddressUse;
import org.hl7.fhir.instance.model.Contact.ContactUse;
import org.hl7.fhir.instance.model.Patient.ContactComponent;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;

public class TestA05Processor extends Hl7TestSupport {
	
	private A05Processor processor;
	
	@Before
	public void before() throws IOException {
		processor = new A05Processor();
		processor.setXsltPath("/uk/org/openeyes/oink/hl7v2/a28ANDa31.xsl");
	}
	
	@Test
	public void testProcessorHandlesValidA05Message() throws Exception {
		testProcessorProducesExpectedOutput(processor, "/hl7v2/A05.txt", "/oinkrequestmessages/A05.json");		
	}
	
	@Ignore
	@Test
	public void buildTestA05FhirBody() throws ParseException, IOException {
		
		Patient p = new Patient();
		p.addIdentifier().setValueSimple("7111111").setSystemSimple("MR");
		p.addIdentifier().setValueSimple("9999999999").setSystemSimple("NHS");
		p.addIdentifier().setValueSimple("7111111").setSystemSimple("PAS");
		p.addIdentifier().setValueSimple("OKIA 346").setSystemSimple("OLNHS");
		p.addIdentifier().setValueSimple("000000MRSTEST").setSystemSimple("COMCA");
		
		
		HumanName name = p.addName();
		name.addFamilySimple("Test");
		name.addGivenSimple("Testdon");
		name.addPrefixSimple("MRS");
		
		p.setBirthDateSimple(new DateAndTime("1970-01-01"));
		CodeableConcept conc = new CodeableConcept();
		conc.addCoding().setCodeSimple("F").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Female");
		p.setGender(conc);
		
		Address add = p.addAddress();
		add.addLineSimple("1 Testing");
		add.addLineSimple("TestLand");
		add.setCitySimple("Testbury");
		add.setZipSimple("SP0 0BW");
		add.setUseSimple(AddressUse.home);
		
		p.addTelecom().setUseSimple(ContactUse.home).setValueSimple("0999 999999");
		p.addTelecom().setUseSimple(ContactUse.work).setValueSimple("0000");
		
		// PID 15??
		
		CodeableConcept mConc = new CodeableConcept();
		mConc.addCoding().setCodeSimple("M").setSystemSimple("http://hl7.org/fhir/vs/marital-status");
		p.setMaritalStatus(mConc);
		
		// PID 17 -- RELIGION
		
		// PID 22 -- Ethnic Group N/A
		
		// PID 26 -- Citizenship
		
		// PID.30
		Boolean isDeceased = new Boolean();
		isDeceased.setValue(false);
		p.setDeceased(isDeceased);
		
		// Set next of kin information
		ContactComponent c = p.addContact();
		HumanName husbandName = new HumanName();
		husbandName.addFamilySimple("Test");
		husbandName.addGivenSimple("Super");
		husbandName.addPrefixSimple("MRS");
		c.setName(husbandName);
		c.addRelationship().addCoding().setCodeSimple("partner").setSystemSimple("http://hl7.org/fhir/vs/patient-contact-relationship");
		Address husbandAddress = new Address();
		husbandAddress.addLineSimple("1 Testing");
		husbandAddress.addLineSimple("TestLand");
		husbandAddress.setCitySimple("Testbury");
		husbandAddress.setZipSimple("SP0 0BW");
		husbandAddress.setUseSimple(AddressUse.home);
		c.setAddress(husbandAddress);
		c.addTelecom().setValueSimple("0999 999999");
		
		// Set second next of kin info
		ContactComponent c2 = p.addContact();
		HumanName c2Name = new HumanName();
		c2Name.addFamilySimple("Test");
		c2Name.addGivenSimple("Super");
		c2Name.addPrefixSimple("MRS");
		
		c2.addRelationship().addCoding().setCodeSimple("partner").setSystemSimple("http://hl7.org/fhir/vs/patient-contact-relationship");
		Address c2Address = new Address();
		c2Address.addLineSimple("1 Testing");
		c2Address.addLineSimple("TestLand");
		c2Address.setCitySimple("TESTBURY");
		c2Address.setZipSimple("SP0 0BW");
		c2Address.setUseSimple(AddressUse.home);
		
		c2.addTelecom().setValueSimple("0999 999999");
		c2.addTelecom().setValueSimple("0000").setUseSimple(ContactUse.work);
		CodeableConcept c2Codable = new CodeableConcept();
		c2Codable.addCoding().setCodeSimple("F").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender");
		c2.setGender(c2Codable);
		
		FhirBody body = new FhirBody(p);
		OINKRequestMessage req = new OINKRequestMessage(null, null, "/Patient", HttpMethod.POST, null, body);
		
		OinkMessageConverter conv = new OinkMessageConverter();
		String s = conv.toJsonString(req);
		Files.write(Paths.get("/Users/Oli/A05.json"), s.getBytes());
		
	}

}
