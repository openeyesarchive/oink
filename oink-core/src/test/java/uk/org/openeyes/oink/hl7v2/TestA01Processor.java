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

public class TestA01Processor extends Hl7TestSupport {
	
	private A01Processor processor;
	
	@Before
	public void before() throws IOException {
		processor = new A01Processor();
		processor.setXsltPath("/uk/org/openeyes/oink/hl7v2/a28ANDa31.xsl");
	}
	
	@Test
	public void testProcessorHandlesValidA01Message() throws Exception {
		testProcessorProducesExpectedOutput(processor, "/hl7v2/A01.txt", "/oinkrequestmessages/A01.json");		
	}
	
	@Ignore
	@Test
	public void buildTestA01FhirBody() throws ParseException, IOException {
		Patient p = new Patient();
		p.addIdentifier().setValueSimple("7111111").setSystemSimple("MR");
		p.addIdentifier().setValueSimple("7111111").setSystemSimple("PAS");
		p.addIdentifier().setValueSimple("9999999999").setSystemSimple("NHS");
		HumanName name = p.addName();
		name.addFamilySimple("Test");
		name.addGivenSimple("Test");
		name.addPrefixSimple("MRS");
		
		p.setBirthDateSimple(new DateAndTime("1970-01-01"));
		CodeableConcept conc = new CodeableConcept();
		conc.addCoding().setCodeSimple("F").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Female");
		p.setGender(conc);
		
		Address add = p.addAddress();
		add.addLineSimple("4 TestLand");
		add.addLineSimple("Test Road");
		add.setCitySimple("TESTBURY");
		add.setStateSimple("Testshire");
		add.setZipSimple("SP0 0WN");
		add.setUseSimple(AddressUse.home);
		
		p.addTelecom().setUseSimple(ContactUse.home).setValueSimple("01722 999999");
		
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
		husbandName.addFamilySimple("Super");
		husbandName.addGivenSimple("Test");
		husbandName.addPrefixSimple("Mr");
		c.setName(husbandName);
		c.addRelationship().addCoding().setCodeSimple("partner").setSystemSimple("http://hl7.org/fhir/vs/patient-contact-relationship");
		Address husbandAddress = new Address();
		husbandAddress.addLineSimple("Testfield");
		husbandAddress.addLineSimple("Test Road");
		husbandAddress.setCitySimple("TESTBURY");
		husbandAddress.setStateSimple("Testshire");
		husbandAddress.setZipSimple("SP5 2AA");
		husbandAddress.setUseSimple(AddressUse.home);
		c.setAddress(husbandAddress);
		
		CodeableConcept husbandCocn = new CodeableConcept();
		husbandCocn.addCoding().setCodeSimple("M").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Male");
		c.setGender(husbandCocn);
		
		FhirBody body = new FhirBody(p);
		OINKRequestMessage req = new OINKRequestMessage(null, null, "/Patient", HttpMethod.POST, null, body);
		
		OinkMessageConverter conv = new OinkMessageConverter();
		String s = conv.toJsonString(req);
		Files.write(Paths.get("/Users/Oli/A01.json"), s.getBytes());
		
	}

}
