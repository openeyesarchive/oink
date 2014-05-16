package uk.org.openeyes.oink.hl7v2;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Boolean;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.String_;
import org.hl7.fhir.instance.model.Address.AddressUse;
import org.hl7.fhir.instance.model.Contact.ContactSystem;
import org.hl7.fhir.instance.model.Contact.ContactUse;
import org.hl7.fhir.instance.model.Patient.ContactComponent;
import org.hl7.fhir.instance.model.Type;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.hl7v2.Hl7TestSupport.NestedResourceIdGenerator;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class TestA31Processor extends Hl7TestSupport {
	
	private A31Processor processor;
	
	@Before
	public void before() throws IOException {
		processor = new A31Processor();
		processor.setXsltPath("/uk/org/openeyes/oink/hl7v2/a28ANDa31.xsl");
	}
		
	@Ignore
	@Test
	public void buildExpectedA312Message() throws ParseException, IOException {
		
		Patient p = new Patient();
		p.addIdentifier().setValueSimple("1(408)-886-66731").setSystemSimple("MR");
		p.addIdentifier().setValueSimple("100001").setSystemSimple("PAS");
		p.addIdentifier().setValueSimple("223821").setSystemSimple("RAD");
		p.addIdentifier().setValueSimple("220134MROBEMORR").setSystemSimple("COMCA");
		p.addIdentifier().setValueSimple("4148734654").setSystemSimple("NHS");
		p.addIdentifier().setValueSimple("GEN 16496").setSystemSimple("PICK");
		
		HumanName name = p.addName();
		name.addFamilySimple("PIKES");
		name.addGivenSimple("RICHIE");
		name.addPrefixSimple("MR");
		
		p.setBirthDateSimple(new DateAndTime("1938-05-27"));
		CodeableConcept conc = new CodeableConcept();
		conc.addCoding().setCodeSimple("M").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Female");
		p.setGender(conc);
		
		Address address = p.addAddress();
		address.addLineSimple("9099 34TH AVE");
		address.addLineSimple("Pound Hill Landford");
		address.setCitySimple("LAURIER");
		address.setZipSimple("SP5 2AA");
		address.setCountrySimple("USA");
		address.setUseSimple(AddressUse.home);
		
		p.addTelecom().setValueSimple("(408)-315-8981").setUseSimple(ContactUse.home);
		p.addTelecom().setValueSimple("(408)-886-6673").setUseSimple(ContactUse.work);
		
		CodeableConcept mConc = new CodeableConcept();
		mConc.addCoding().setCodeSimple("M").setSystemSimple("http://hl7.org/fhir/vs/marital-status");
		p.setMaritalStatus(mConc);
		
		Boolean isDeceased = new Boolean();
		isDeceased.setValue(false);
		p.setDeceased(isDeceased);
		
		// Set next of kin information
		ContactComponent c = p.addContact();
		HumanName wifeName = new HumanName();
		wifeName.addFamilySimple("EWELL");
		wifeName.addGivenSimple("JOAQUIN");
		wifeName.addPrefixSimple("MRS");
		c.setName(wifeName);
		c.addRelationship().addCoding().setCodeSimple("partner").setSystemSimple("http://hl7.org/fhir/vs/patient-contact-relationship");
		Address wifeAddress = new Address();
		wifeAddress.addLineSimple("7333 ASHGROVE WAY");
		wifeAddress.addLineSimple("Pound Hill Landford");
		wifeAddress.setCitySimple("LACEY");
		wifeAddress.setZipSimple("SP5 2AA");
		wifeAddress.setCountrySimple("USA");
		wifeAddress.setUseSimple(AddressUse.home);
		c.setAddress(wifeAddress);
		
		CodeableConcept wifeConc = new CodeableConcept();
		wifeConc.addCoding().setCodeSimple("F").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Female");
		c.setGender(wifeConc);
		
		NestedResourceIdGenerator idGenerator = new NestedResourceIdGenerator();
		
		// PD1-3 to managingOrganization
		List<Resource> containedResources = p.getContained();
		Organization org = new Organization();
		org.setNameSimple("The Surgery,Common Road,Whiteparish,Salisbury,Wiltshire,SP5 2SU");
		String xmlId = idGenerator.getNext();
		org.setXmlId(xmlId);
		containedResources.add(org);
		ResourceReference ref = new ResourceReference();
		ref.setReferenceSimple(xmlId);
		p.setManagingOrganization(ref);
		
		// PD1-4
		Practitioner pract = new Practitioner();
		pract.addIdentifier().setValueSimple("G8710776");
		HumanName practName = new HumanName();
		practName.addFamilySimple("Gotham");
		practName.addGivenSimple("C R");
		practName.addPrefixSimple("DR");
		pract.setName(practName);
		String practXmlId = idGenerator.getNext();
		pract.setXmlId(practXmlId);
		containedResources.add(pract);
		p.addCareProvider().setReferenceSimple(practXmlId);
		
		FhirBody body = new FhirBody(p);
		OINKRequestMessage req = new OINKRequestMessage(null, null, "/Patient", HttpMethod.POST, null, body);
		
		OinkMessageConverter conv = new OinkMessageConverter();
		String s = conv.toJsonString(req);
		Files.write(Paths.get("/Users/Oli/A31-2.json"), s.getBytes());
	}
	
	@Test
	public void testProcessorHandlesValidA31_2Message() throws Exception {
		
		testProcessorProducesExpectedOutput(processor, "/hl7v2/A31-2.txt", "/oinkrequestmessages/A31-2.json");		
	}

}
