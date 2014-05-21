package uk.org.openeyes.oink.hl7v2;

import java.util.List;

import org.apache.camel.Exchange;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.exception.OinkException;

public class ADTProcessor extends Hl7v2Processor {

	private final static Logger log = LoggerFactory
			.getLogger(ADTProcessor.class);

	@Override
	public void postResourcesInBundle(AtomFeed bundle, Exchange ex)
			throws OinkException {

		// Extract Patient from Bundle
		Patient patient = extractPatient(bundle);

		String location = visit(patient, bundle, ex);
		log.info("Patient posted to server with url:" + location);

	}

	/**
	 * Processes a Patient resource in the bundle. Making sure all of the other
	 * resources referenced by the Patient exist in the destination server
	 * before posting the patient itself.
	 */
	private String visit(Patient p, AtomFeed bundle, Exchange ex)
			throws OinkException {

		// Handle Care Providers
		for (ResourceReference resourceRef : p.getCareProvider()) {
			AtomEntry<? extends Resource> resource = bundle.getById(resourceRef
					.getReferenceSimple());
			if (resource == null) {
				log.warn("Couldn't find resource "
						+ resourceRef.getReferenceSimple() + " in bundle");
				continue;
			} else if (resource.getResource().getResourceType()
					.equals(ResourceType.Practitioner)) {
				Practitioner practitioner = (Practitioner) resource
						.getResource();
				String absoluteUrl = visit(practitioner, bundle, ex);
				resourceRef.setReferenceSimple(absoluteUrl);
			} else if (resource.getResource().getResourceType()
					.equals(ResourceType.Organization)) {
				Organization org = (Organization) resource.getResource();
				String absoluteUrl = visit(org, bundle, ex);
				resourceRef.setReferenceSimple(absoluteUrl);
			} else {
				log.error("A care provider was referenced which isn't a Practitioner or an Organization");
				throw new OinkException();
			}
		}
		// Handle Managing Organisation
		ResourceReference managingOrgRef = p.getManagingOrganization();
		if (managingOrgRef != null) {
			AtomEntry<? extends Resource> resource = bundle
					.getById(managingOrgRef.getReferenceSimple());
			if (resource.getResource().getResourceType()
					.equals(ResourceType.Organization)) {
				Organization org = (Organization) resource.getResource();
				String absoluteUrl = visit(org, bundle, ex);
				managingOrgRef.setReferenceSimple(absoluteUrl);
			} else {
				log.error("A Managing Organization was referenced which isn't an Organization");
			}
		}

		// POST Patient
		String location = postResource(p, ex);
		return location;
	}

	/**
	 * Processes an Organization in a bundle. Location and PartOf are not
	 * catered for yet.
	 */
	private String visit(Organization org, AtomFeed bundle, Exchange ex) {

		// Search for organization
		List<Identifier> ids = org.getIdentifier();
		String location = searchForResourceByIdentifiers(org, ids, ex);

		if (location != null) {
			return location;
		}

		// Else. Post organization
		return postResource(org, ex);

	}

	/**
	 * Processes an Practitioner in a bundle. Making sure all resources
	 * referenced by the Practitioner exist on the end server before posting the
	 * Practitioner itself.
	 */
	private String visit(Practitioner p, AtomFeed bundle, Exchange ex) {
		ResourceReference orgRef = p.getOrganization();
		if (orgRef != null) {
			AtomEntry<? extends Resource> resource = bundle.getById(orgRef
					.getReferenceSimple());
			if (resource.getResource().getResourceType()
					.equals(ResourceType.Organization)) {
				Organization org = (Organization) resource.getResource();
				String absoluteUrl = visit(org, bundle, ex);
				orgRef.setReferenceSimple(absoluteUrl);
			} else {
				log.error("An Organization was referenced which isn't an Organization");
			}
		}

		// Search for practitioner
		List<Identifier> ids = p.getIdentifier();
		String location = searchForResourceByIdentifiers(p, ids, ex);

		if (location != null) {
			return location;
		}

		// Else. Post practitioner
		return postResource(p, ex);
	}

	/**
	 * Searches for the single Patient entry that should be in the bundle. Warns if another one is present.
	 */
	private Patient extractPatient(AtomFeed bundle) {
		Patient p = null;
		for (AtomEntry<? extends Resource> entry : bundle.getEntryList()) {
			if (entry.getResource().getResourceType()
					.equals(ResourceType.Patient)) {
				if (p != null) {
					log.warn("Multiple patients found in Bundle, using the first one");
					return p;
				} else {
					p = (Patient) entry.getResource();
				}
			}
		}
		return p;
	}

}
