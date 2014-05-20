package uk.org.openeyes.oink.hl7v2;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

import javassist.tools.framedump;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Boolean;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Address.AddressUse;
import org.hl7.fhir.instance.model.Contact.ContactUse;
import org.hl7.fhir.instance.model.Patient.ContactComponent;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;

import ca.uhn.hl7v2.model.Message;
import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;

public class TestA01Processor extends Hl7TestSupport {
	
	private ADTProcessor processor;
	
	@Before
	public void before() throws IOException {
		processor = new ADTProcessor();
		org.springframework.core.io.Resource r = new ClassPathResource("/uk/org/openeyes/oink/hl7v2/a28.xsl");
		processor.setXsltPath(r);	}
	
	@Test
	public void testProcessorHandlesValidA01Message() throws Exception {
		Message hl7Message = loadMessage("/hl7v2/A01.txt");
		
		Exchange ex = mock(Exchange.class);
		CamelContext ctx = mock(CamelContext.class);
		ProducerTemplate prod = mock(ProducerTemplate.class);
		
		when(ex.getContext()).thenReturn(ctx);
		when(ctx.createProducerTemplate()).thenReturn(prod);
		
		// Organisation Care Provider
		OINKRequestMessage orgQuery = new OINKRequestMessage();
		orgQuery.setResourcePath("/Organisation");
		orgQuery.setMethod(HttpMethod.GET);
		orgQuery.setParameters("identifier=GPPRC|J83000");

		OINKResponseMessage orgQueryResponse = new OINKResponseMessage();
		AtomFeed orgQueryResponseBundle = new AtomFeed();
		orgQueryResponse.setBody(new FhirBody(orgQueryResponseBundle));
		
		when(prod.requestBody("director:rabbit-rpc", orgQuery)).thenReturn(orgQueryResponse);
		
		OINKRequestMessage orgPost = new OINKRequestMessage();
		orgPost.setResourcePath("/Organisation");
		orgPost.setMethod(HttpMethod.POST);
		Organization org = new Organization();
		org.addIdentifier().setSystemSimple("GPPRC").setValueSimple("J83000");
		orgPost.setBody(new FhirBody(org));
		
		OINKResponseMessage orgPostResponse = new OINKResponseMessage();
		orgPostResponse.setStatus(200);
		orgPostResponse.setLocationHeader("http://fhir.com/fhir/Organisation/1/_history/1");
		
		when(prod.requestBody("director:rabbit-rpc", orgPost)).thenReturn(orgPostResponse);
		
		// Practitioner Care Provider
		OINKRequestMessage practQuery = new OINKRequestMessage();
		practQuery.setResourcePath("/Practitioner");
		practQuery.setMethod(HttpMethod.GET);
		practQuery.setParameters("identifier=G1701835");

		OINKResponseMessage practQueryResponse = new OINKResponseMessage();
		AtomFeed practQueryResponseBundle = new AtomFeed();
		practQueryResponse.setBody(new FhirBody(practQueryResponseBundle));
		
		when(prod.requestBody("director:rabbit-rpc", practQuery)).thenReturn(practQueryResponse);
		
		OINKRequestMessage practPost = new OINKRequestMessage();
		practPost.setResourcePath("/Practitioner");
		practPost.setMethod(HttpMethod.POST);
		Practitioner pract = new Practitioner();
		pract.addIdentifier().setValueSimple("G1701835");
		HumanName practName = new HumanName();
		practName.addFamilySimple("Test");
		practName.addGivenSimple("T");
		practName.addPrefixSimple("Dr");
		pract.setName(practName);
		
		practPost.setBody(new FhirBody(org));
		
		OINKResponseMessage practPostResponse = new OINKResponseMessage();
		practPostResponse.setStatus(200);
		practPostResponse.setLocationHeader("http://fhir.com/fhir/Practitioner/1/_history/1");
		
		when(prod.requestBody("director:rabbit-rpc", practPost)).thenReturn(practPostResponse);		
		
		OINKRequestMessage patientPost = new OINKRequestMessage();
		patientPost.setMethod(HttpMethod.POST);
		patientPost.setResourcePath("/Patient");

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
		
		// NK1 
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
		
		
		// PD1-3 to managingOrganization
		p.addCareProvider().setReferenceSimple("http://fhir.com/fhir/Organisation/1/_history/1");
		
		// PD1-4
		p.addCareProvider().setReferenceSimple("http://fhir.com/fhir/Practitioner/1/_history/1");
		
		FhirBody body = new FhirBody(p);
		patientPost.setBody(body);
		
		OINKResponseMessage patResponse = new OINKResponseMessage();
		patResponse.setStatus(200);
		patResponse.setLocationHeader("http://fhir.com/fhir/Patient/1/_history/1");

		processor.process(hl7Message, ex);
		
		verify(prod).requestBody("direct:rabbit-rpc", orgQuery);
		verify(prod).requestBody("direct:rabbit-rpc", orgPost);
		verify(prod).requestBody("direct:rabbit-rpc", practQuery);
		verify(prod).requestBody("direct:rabbit-rpc", practPost);
		verify(prod).requestBody("direct:rabbit-rpc", patientPost);
		
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
		
		// NK1 
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
		
		NestedResourceIdGenerator idGenerator = new NestedResourceIdGenerator();
		
		// PD1-3 to managingOrganization
		List<Resource> containedResources = p.getContained();
		Organization org = new Organization();
		org.setNameSimple("Test Medical Ctr,Test Road,Testbury,Testshire,SP0 0WN");
		String xmlId = idGenerator.getNext();
		org.setXmlId(xmlId);
		containedResources.add(org);
		ResourceReference ref = new ResourceReference();
		ref.setReferenceSimple(xmlId);
		p.setManagingOrganization(ref);
		
		// PD1-4
		Practitioner pract = new Practitioner();
		pract.addIdentifier().setValueSimple("G1701835");
		HumanName practName = new HumanName();
		practName.addFamilySimple("Test");
		practName.addGivenSimple("T");
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
		Files.write(Paths.get("/Users/Oli/A01.json"), s.getBytes());
		
	}

}
