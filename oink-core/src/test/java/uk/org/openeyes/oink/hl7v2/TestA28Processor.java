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
import uk.org.openeyes.oink.messaging.OinkMessageConverter;
import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

public class TestA28Processor extends Hl7TestSupport {
	
	private A28Processor processor;
	
	@Before
	public void before() throws IOException {
		processor = new A28Processor();
		processor.setXsltPath("/uk/org/openeyes/oink/hl7v2/a28ANDa31.xsl");
	}
	
	/**
	 * Builds an Expected OINKRequestMessage from scratch based on a28-1.xml
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void buildExampleA281FhirBody() throws Exception {
		Patient patient = new Patient();
		List<Identifier> identifiers = patient.getIdentifier();
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
		HumanName name = patient.addName();
		name.addFamily().setValue("DEMIRJIAN");
		name.addGiven().setValue("ELEANORA");
		name.addPrefix().setValue("MRS");
		
		// PID.7
		patient.setBirthDateSimple(new DateAndTime("1955-02-16"));
		
		// PID.8
		CodeableConcept conc = new CodeableConcept();
		conc.addCoding().setCodeSimple("F").setSystemSimple("http://hl7.org/fhir/v3/AdministrativeGender").setDisplaySimple("Female");
		patient.setGender(conc);
		
		// PID.11
		Address address = patient.addAddress();
		address.addLineSimple("6579 21ST AVE");
		address.addLineSimple("Long Street");
		address.setCitySimple("WALNUT CREEK");
		address.setStateSimple("CA");
		address.setZipSimple("DT9 3DD");
		address.setCountrySimple("USA");
		address.setUseSimple(AddressUse.home);
		
		// PID.13
		Contact contact = patient.addTelecom();
		contact.setSystemSimple(ContactSystem.phone);
		contact.setValueSimple("(408)-960-2444");
		
		// PID.15
		patient.addCommunication().setTextSimple("NSP");
		
		// PID.30
		Boolean isDeceased = new Boolean();
		isDeceased.setValue(false);
		patient.setDeceased(isDeceased);
		
		// PD1.3
//		Organization organization = new Organization();
//		Identifier orgId = organization.addIdentifier();
//		orgId.setValueSimple("Y90001").setLabelSimple("Y90001").setSystemSimple("GPPRC");
//		ResourceReference ref = new ResourceReference();

		// PD1.4
//		JsonComposer comp = new JsonComposer();
//		File file = new File("/Users/Oli/out.json");
//		FileOutputStream fop = new FileOutputStream(file);
//		comp.compose(fop, patient, true);
//		fop.close();
		FhirBody body = new FhirBody(patient);
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
