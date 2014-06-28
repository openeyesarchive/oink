package uk.org.openeyes.oink.itest.openeyes;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.datagen.domain.Person;
import uk.org.openeyes.oink.datagen.generators.person.PersonGenerator;
import uk.org.openeyes.oink.datagen.generators.person.PersonGeneratorFactory;
import uk.org.openeyes.oink.it.ITSupport;
import ca.uhn.fhir.model.dstu.composite.AddressDt;
import ca.uhn.fhir.model.dstu.composite.ContactDt;
import ca.uhn.fhir.model.dstu.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.dstu.valueset.AddressUseEnum;
import ca.uhn.fhir.model.dstu.valueset.AdministrativeGenderCodesEnum;
import ca.uhn.fhir.model.dstu.valueset.ContactSystemEnum;
import ca.uhn.fhir.model.dstu.valueset.ContactUseEnum;
import ca.uhn.fhir.model.dstu.valueset.IdentifierUseEnum;
import ca.uhn.fhir.model.dstu.valueset.NameUseEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.IGenericClient;

public class ITOpenEyesFHIRApi {

	private static Logger logger = LoggerFactory
			.getLogger(ITOpenEyesFHIRApi.class);

	private static Properties proxyProps;

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException {
		proxyProps = ITSupport.getPropertiesBySystemProperty("it.proxy.config");
	}

	@Test
	public void test() throws Exception {

		PersonGenerator g = PersonGeneratorFactory.getInstance("uk");

		List<Person> persons = g.generate(1000);
		List<Patient> patients = new ArrayList<Patient>();

		for (Person p : persons) {

			Patient patient = new Patient();

			// Add an MRN (a patient identifier)
			IdentifierDt id = patient.addIdentifier();
			id.setLabel("Hospital Number");
			id.setValue(p.getIdentifiers().get(0).getValue());

			id = patient.addIdentifier();
			id.setUse(IdentifierUseEnum.OFFICIAL);
			id.setLabel("NHS Number");
			id.setSystem("http://www.datadictionary.nhs.uk/data_dictionary/attributes/n/nhs/nhs_number_de.asp");
			id.setValue(p.getIdentifiers().get(1).getValue());

			// Add a name
			HumanNameDt name = patient.addName();
			name.setUse(NameUseEnum.USUAL);
			name.addFamily(p.getLastName());
			name.addGiven(p.getFirstName());
			name.addPrefix(p.getPrefix());

			// Gender
			patient.setGender(AdministrativeGenderCodesEnum.valueOf(p
					.getGender()));

			// Birth date
			patient.setBirthDate(new DateTimeDt(p.getDateOfBirth().toString(
					"yyyy-mm-dd")));

			// Add phone
			ContactDt phone = patient.addTelecom();
			phone.setSystem(ContactSystemEnum.PHONE);
			phone.setUse(ContactUseEnum.HOME);
			phone.setValue(p.getTelecoms().get(0).getValue());

			// Add address
			AddressDt addr = patient.addAddress();
			addr.setUse(AddressUseEnum.HOME);
			addr.addLine(p.getAddresses().get(0).getLine1());
			addr.setCity(p.getAddresses().get(0).getLine2());
			addr.setZip(p.getAddresses().get(0).getZipCode());
			addr.setCountry("United Kingdom");

			patient.addCareProvider().setReference("Organization/prac-1");
			ResourceReferenceDt ref = new ResourceReferenceDt();
			ref.setReference("Organization/gp-1");
			patient.setManagingOrganization(ref);

			patients.add(patient);
		}

		final IGenericClient client = ITSupport.buildHapiClientForProxy(proxyProps);

		int threads = Integer.parseInt(System.getProperty(
				"oink.it.openeyes.threads", "10"));
		final Semaphore semaphore = new Semaphore(threads);
		ExecutorService executorService = Executors.newFixedThreadPool(threads);

		final DateTime dtStart = new DateTime();
		final AtomicInteger processed = new AtomicInteger();

		for (final Patient patient : patients) {

			semaphore.acquire();
			executorService.execute(new Runnable() {
				public void run() {
					MethodOutcome mo = client.create(patient);
					String result = mo.getOperationOutcome().getId().getValue();
					assertEquals("200", result);
					
					processed.incrementAndGet();
					
					DateTime dtNow = new DateTime();
					Interval interval = new Interval(dtStart, dtNow);
					double rate = ((double) processed.get())
							/ (((double) interval.toDurationMillis()) / 1000.0);

					logger.debug("[{}] {} msgs/s {} - {}", processed, String.format("%1$,.1f", rate),
							patient.getNameFirstRep().getGivenAsSingleString(),
							patient.getNameFirstRep().getFamilyAsSingleString());

					semaphore.release();
				}
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(24, TimeUnit.HOURS);
		DateTime dtEnd = new DateTime();

		Interval interval = new Interval(dtStart, dtEnd);

		double rate = ((double) processed.get())
				/ (((double) interval.toDurationMillis()) / 1000.0);

		logger.info("Patients: {}", patients.size());
		logger.info("Processed {} messages in {} seconds", processed, String
				.format("%1$,.1f",
						((double) interval.toDurationMillis()) / 1000.0));
		logger.info("Rate: {} messages/sec", String.format("%1$,.1f", rate));

	}
}
