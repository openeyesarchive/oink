package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Boolean;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.Address.AddressUse;
import org.hl7.fhir.instance.model.Contact.ContactUse;
import org.hl7.fhir.instance.model.Patient.ContactComponent;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.hl7v2.Hl7TestSupport.NestedResourceIdGenerator;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;

public class TestA40Processor extends Hl7TestSupport {
	
	private A40Processor processor;
	
	@Before
	public void before() throws IOException {
		processor = new A40Processor();
		processor.setXsltPath("/uk/org/openeyes/oink/hl7v2/a28ANDa31.xsl");
	}
	
	@Test
	public void testProcessorHandlesValidA40Message() throws Exception {
		testProcessorProducesExpectedOutput(processor, "/hl7v2/A40-1.txt", "/oinkrequestmessages/A40-1.json");		
	}
	
	@Ignore
	@Test
	public void buildTestA40_1FhirBody() throws ParseException, IOException {
		
		
		Patient p = new Patient();
		p.addIdentifier().setValueSimple("100001").setSystemSimple("MAJOR");
		p.addIdentifier().setValueSimple("0753332").setSystemSimple("PRMIN");
		HumanName name = p.addName();
		name.addFamilySimple("STETSON");
		name.addGivenSimple("DOROTHA");
		name.addPrefixSimple("MISS");
		
		p.setBirthDateSimple(new DateAndTime("1950-05-25"));
		CodeableConcept conc = new CodeableConcept();
		conc.addCoding().setCodeSimple("F").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Female");
		p.setGender(conc);
		
		HumanName alias = p.addName();
		alias.addFamilySimple("PAIN");
		alias.addGivenSimple("DOROTHA");
		
		
		Address add = p.addAddress();
		add.addLineSimple("2202 BLOSSOM AVE #10");
		add.addLineSimple("ALDERHOLT");
		add.setCitySimple("TORRANCE");
		add.setStateSimple("CA");
		add.setZipSimple("SP6 3ER");
		add.setCountrySimple("USA");
		add.setUseSimple(AddressUse.home);
		
		p.addTelecom().setUseSimple(ContactUse.home).setValueSimple("(408)-783-7332");
		
		// PID 15??
		
		CodeableConcept mConc = new CodeableConcept();
		mConc.addCoding().setCodeSimple("U").setSystemSimple("http://hl7.org/fhir/vs/marital-status");
		p.setMaritalStatus(mConc);
		
		// PID 17 -- RELIGION
		
		// PID 19 Social Security Number?
		
		// PID 20 Drivers License Number?
		
		// PID 22 -- Ethnic Group N/A
		
		// PID 26 -- Citizenship
		
		// PID.30
		Boolean isDeceased = new Boolean();
		isDeceased.setValue(false);
		p.setDeceased(isDeceased);
		
		FhirBody body = new FhirBody(p);
		OINKRequestMessage req = new OINKRequestMessage(null, null, "/Patient", HttpMethod.POST, null, body);
		
		OinkMessageConverter conv = new OinkMessageConverter();
		String s = conv.toJsonString(req);
		Files.write(Paths.get("/Users/Oli/A40-1.json"), s.getBytes());
		
	}

}
