package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Boolean;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.String_;
import org.hl7.fhir.instance.model.Address.AddressUse;
import org.hl7.fhir.instance.model.Contact.ContactSystem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;

public class TestA28Processor extends Hl7TestSupport {
	
	private ADTProcessor processor;
	
	@Before
	public void before() throws IOException {
		processor = new ADTProcessor();
		org.springframework.core.io.Resource r = new ClassPathResource("/uk/org/openeyes/oink/hl7v2/a28.xsl");
		processor.setXsltPath(r);
	}
	
	/**
	 * Builds an Expected OINKRequestMessage from scratch based on a28-1.xml
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void buildExampleA281FhirBody() throws Exception {
		Patient p = new Patient();
		List<Identifier> identifiers = p.getIdentifier();
		// PID.3
		Identifier id1 = new Identifier();
		
		String_ idValue = new String_();
		idValue.setValue("100001");
		id1.setValue(idValue);
		
		id1.setSystemSimple("MR");
		
//		ResourceReference ref = new ResourceReference();
//		ref.setReferenceSimple("RNZ02");
//		id1.setAssigner(ref);
		identifiers.add(id1);
		
		// PID.3
		Identifier id2 = new Identifier();
		String_ idValue2 = new String_();
		idValue2.setValue("100001");
		id2.setValue(idValue);	
		
		id2.setSystemSimple("PAS");

		identifiers.add(id2);
		
		// PID.5
		HumanName name = p.addName();
		name.addFamily().setValue("DEMIRJIAN");
		name.addGiven().setValue("ELEANORA");
		name.addPrefix().setValue("MRS");
		
		// PID.7
		p.setBirthDateSimple(new DateAndTime("1955-02-16"));
		
		// PID.8
		CodeableConcept conc = new CodeableConcept();
		conc.addCoding().setCodeSimple("F").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Female");
		p.setGender(conc);
		
		// PID.11
		Address address = p.addAddress();
		address.addLineSimple("6579 21ST AVE");
		address.addLineSimple("Long Street");
		address.setCitySimple("WALNUT CREEK");
		address.setStateSimple("CA");
		address.setZipSimple("DT9 3DD");
		address.setCountrySimple("USA");
		address.setUseSimple(AddressUse.home);
		
		// PID.13
		Contact contact = p.addTelecom();
		contact.setSystemSimple(ContactSystem.phone);
		contact.setValueSimple("(408)-960-2444");
		
		// PID.15
		//p.addCommunication().setTextSimple("NSP");
		
		// PID.30
		Boolean isDeceased = new Boolean();
		isDeceased.setValue(false);
		p.setDeceased(isDeceased);
		
		NestedResourceIdGenerator idGenerator = new NestedResourceIdGenerator();
		
		// PD1-3 to managingOrganization??
		List<Resource> containedResources = p.getContained();

		
		// PD1-4
		Practitioner pract = new Practitioner();
		pract.addIdentifier().setValueSimple("G9999998");
		HumanName practName = new HumanName();
		practName.addFamilySimple("Unknown");
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
		Files.write(Paths.get("/Users/Oli/A28-1.json"), s.getBytes());
		
	}
	
	@Test
	public void testProcessorHandlesValidA28Message() throws Exception {
		testProcessorProducesExpectedOutput(processor, "/hl7v2/A28-1.txt", "/oinkrequestmessages/A28-1.json");		
	}

}
