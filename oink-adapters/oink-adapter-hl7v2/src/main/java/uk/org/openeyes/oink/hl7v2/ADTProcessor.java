/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.hl7v2;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Contact.ContactSystem;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.HumanName.NameUse;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.exception.OinkException;

/**
 * An extension of the {@link Hl7v2Processor} for processing ADT messages
 * containing Patient Information from a remote HL7v2 server.
 * 
 */
public class ADTProcessor extends Hl7v2Processor {

	private final static Logger log = LoggerFactory.getLogger(ADTProcessor.class);

	private boolean resolveCareProvider = true;
	private boolean resolveManagingOrganization = true;

	@Override
	public void processResourcesInBundle(AtomFeed bundle, Exchange ex) throws OinkException {

		// Extract Patient from Bundle
		Patient patient = extractPatient(bundle);

		if (patient == null) {
			final String errorMsg = "No Patient Resource was found inside the generated bundle";
			log.error(errorMsg);
			throw new OinkException(errorMsg);
		}

		String location = postResourceAndReferencedResources(patient, bundle, ex);
		log.info("Patient posted to server with url:" + location);
	}

	/**
	 * Processes a Patient resource in the bundle. Making sure all of the other
	 * resources referenced by the Patient exist in the destination server
	 * before posting the patient itself.
	 */
	private String postResourceAndReferencedResources(Patient p, AtomFeed bundle, Exchange ex) throws OinkException {

		log.debug("Posting Patient and all associated resources as necessary");

		if (resolveCareProvider) {
			// Handle Care Providers
			for (ResourceReference resourceRef : p.getCareProvider()) {
				AtomEntry<? extends Resource> resource = bundle.getById(resourceRef.getReferenceSimple());
				if (resource == null) {
					log.warn("Couldn't find resource " + resourceRef.getReferenceSimple() + " in bundle");
					continue;
				} else if (resource.getResource().getResourceType().equals(ResourceType.Practitioner)) {
					Practitioner practitioner = (Practitioner) resource.getResource();
					String absoluteUrl = postResourceAndReferencedResources(practitioner, bundle, ex);
					String relativeUrl = extractResourceRelativeUrlFromLocation(absoluteUrl, "Practitioner");
					resourceRef.setReferenceSimple(relativeUrl);
				} else if (resource.getResource().getResourceType().equals(ResourceType.Organization)) {
					Organization org = (Organization) resource.getResource();
					String absoluteUrl = postResourceAndReferencedResources(org, bundle, ex);
					String relativeUrl = extractResourceRelativeUrlFromLocation(absoluteUrl, "Organization");
					resourceRef.setReferenceSimple(relativeUrl);
				} else {
					log.error("A care provider was referenced which isn't a Practitioner or an Organization");
					throw new OinkException();
				}
			}
		} else {
			// Otherwise ensure empty
			p.getCareProvider().clear();
		}

		if (resolveManagingOrganization) {
			// Handle Managing Organisation
			ResourceReference managingOrgRef = p.getManagingOrganization();
			if (managingOrgRef != null) {
				AtomEntry<? extends Resource> resource = bundle.getById(managingOrgRef.getReferenceSimple());
				if (resource.getResource().getResourceType().equals(ResourceType.Organization)) {
					Organization org = (Organization) resource.getResource();
					String absoluteUrl = postResourceAndReferencedResources(org, bundle, ex);
					String relativeUrl = extractResourceRelativeUrlFromLocation(absoluteUrl, "Organization");
					managingOrgRef.setReferenceSimple(relativeUrl);
					log.debug("Patient's managing org set to " + managingOrgRef.getReferenceSimple());
				} else {
					log.error("A Managing Organization was referenced which isn't an Organization");
				}
			}
		} else {
			// Otherwise ensure empty
			p.setManagingOrganization(null);
		}

		// OpenEyes QUICKFIX: Set Family use to usual
		for (HumanName name : p.getName()) {
			if (name.getUseSimple() == null) {
				log.warn("OINK-43 Manually forcing patient's name use to be usual");
				name.setUseSimple(NameUse.usual);
			}
		}

		// OpenEyes QUICKFIX: Set Phone system
		for (Contact contact : p.getTelecom()) {
			if (contact.getSystemSimple() == null) {
				log.warn("OINK-44 Manually forcing patient's phone system to be phone");
				contact.setSystemSimple(ContactSystem.phone);
			}
		}

		// OpenEyes QUICKFIX: Set Country to United Kingdom
		for (Address address : p.getAddress()) {
			log.warn("OINK-45 Manually forcing patient's addresses to be United Kingdom");
			address.setCountrySimple("United Kingdom");
		}

		// Remap patient identifiers
		remapPatientIdentifiers(p.getIdentifier());

		// OpenEyes QUICKFIX: Remove managingOrgRefs
		if (resolveManagingOrganization || resolveCareProvider) {
			log.warn("OINK-48 Manually moving ManagingOrgRef to CareProvider (OpenEyes does not support ManagingOrg)");
			p.getCareProvider().add(p.getManagingOrganization());
			p.setManagingOrganization(null);
		}

		// POST Patient
		String location = postResource(p, ex);
		return location;
	}

	public static String extractResourceRelativeUrlFromLocation(String location, String resource) {
		log.debug("Extracting relative URL from " + location);
		Pattern p = Pattern.compile("(.*)?/(" + resource + "/[^/]+)(/.*)?");
		Matcher m = p.matcher(location);
		boolean b = m.matches();
		if (!b) {
			log.warn("No matches found");
		}
		return m.group(2);
	}

	/**
	 * Processes an Organization in a bundle. Location and PartOf are not
	 * catered for yet.
	 * 
	 * @throws OinkException
	 */
	private String postResourceAndReferencedResources(Organization org, AtomFeed bundle, Exchange ex) throws OinkException {

		// Remap identifiers
		List<Identifier> ids = org.getIdentifier();
		remapOrganizationIdentifiers(ids);

		// Ensure a system code is set when there is only one identifier
		if (ids.size() == 1 && !StringUtils.hasText(ids.get(0).getSystemSimple())) {
			setDefaultOrganizationIdentifierSystemType(ids.get(0));
		}

		// Search for organization
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
	 * 
	 * @throws OinkException
	 */
	private String postResourceAndReferencedResources(Practitioner p, AtomFeed bundle, Exchange ex) throws OinkException {
		ResourceReference orgRef = p.getOrganization();
		if (orgRef != null) {
			AtomEntry<? extends Resource> resource = bundle.getById(orgRef.getReferenceSimple());
			if (resource.getResource().getResourceType().equals(ResourceType.Organization)) {
				Organization org = (Organization) resource.getResource();
				String absoluteUrl = postResourceAndReferencedResources(org, bundle, ex);
				orgRef.setReferenceSimple(absoluteUrl);
			} else {
				log.error("An Organization was referenced which isn't an Organization");
			}
		}

		// Remap identifiers
		List<Identifier> ids = p.getIdentifier();
		remapPractitionerIdentifiers(ids);

		// Ensure a system code is set when there is only one identifier
		if (ids.size() == 1 && !StringUtils.hasText(ids.get(0).getSystemSimple())) {
			setDefaultPractitionerIdentifierSystemType(ids.get(0));
		}

		// Search for practitioner
		String location = searchForResourceByIdentifiers(p, ids, ex);

		// OPENEYES QUICKFIX: Add human name . use
		log.warn("Manually setting use value for Practioner's name");
		p.getName().setUseSimple(NameUse.usual);

		// OPENEYES QUICKFIX: Remove reference to parent organisation
		log.warn("Manually removing reference to Organization");
		p.setOrganization(null);

		if (location != null) {
			return location;
		}

		// Else. Post practitioner
		return postResource(p, ex);
	}

	/**
	 * Searches for the single Patient entry that should be in the bundle. Warns
	 * if another one is present.
	 */
	private Patient extractPatient(AtomFeed bundle) {
		Patient p = null;
		for (AtomEntry<? extends Resource> entry : bundle.getEntryList()) {
			if (entry.getResource().getResourceType().equals(ResourceType.Patient)) {
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

	/**
	 * Searches for a resource over OINK using the resource's identifiers.
	 * Expects a single entry in the results.
	 * 
	 * @throws OinkException
	 */
	public String searchForResourceByIdentifiers(Resource resource, List<Identifier> ids, Exchange ex) throws OinkException {

		// Build OINKRequestMessage for Query
		OINKRequestMessage query = buildSearchRequestMessage(resource, ids);

		String resourceName = resource.getResourceType().toString();
		log.debug("Searching for " + resourceName + " using the message " + query.toString());

		CamelContext ctx = ex.getContext();
		ProducerTemplate prod = ctx.createProducerTemplate();
		OINKResponseMessage resp = prod.requestBody("direct:rabbit-rpc", query, OINKResponseMessage.class);

		int status = resp.getStatus();

		log.debug("Response had code " + status);

		if (status == 200) {
			AtomFeed bundle = resp.getBody().getBundle();
			if (bundle == null || bundle.getEntryList().isEmpty()) {
				return null;
			} else if (bundle.getEntryList().size() > 1) {
				throw new OinkException("Multiple possible entries found for resource. Cannot be trusted to choose a single one.");
			} else {
				AtomEntry<? extends Resource> entry = bundle.getEntryList().get(0);
				return entry.getId();
			}
		} else {
			throw new OinkException("A preliminary search for a resource " + resourceName + " resulted in status " + status);
		}

	}

	public OINKRequestMessage buildSearchRequestMessage(Resource resource, List<Identifier> ids) {
		// Build OINKRequestMessage for Query
		OINKRequestMessage query = new OINKRequestMessage();
		String resourceName = resource.getResourceType().toString();
		query.setResourcePath("/" + resourceName);
		query.setMethod(HttpMethod.GET);

		// Build search query
		StringBuilder sb = new StringBuilder();
		sb.append("identifier=");
		Iterator<Identifier> iter = ids.iterator();
		while (iter.hasNext()) {
			Identifier id = iter.next();
			if (id.getSystemSimple() != null && !id.getSystemSimple().isEmpty()) {
				sb.append(id.getSystemSimple());
				sb.append("|");
			}
			sb.append(id.getValueSimple());
			if (iter.hasNext()) {
				sb.append(",");
			}
		}

		query.setParameters(sb.toString());
		return query;
	}

	public String postResource(Resource resource, Exchange ex) throws OinkException {

		// Build OINKRequestMessage for Query
		OINKRequestMessage query = buildPostRequestMessage(resource);

		CamelContext ctx = ex.getContext();
		ProducerTemplate prod = ctx.createProducerTemplate();
		OINKResponseMessage resp = prod.requestBody("direct:rabbit-rpc", query, OINKResponseMessage.class);

		int status = resp.getStatus();

		if (status / 100 != 2) {
			throw new OinkException("Failure to post " + resource.getResourceType().toString() + ", response was " + status);
		} else {
			log.debug("Resource of type:" + resource.getResourceType().toString() + " was posted with status " + status);
		}

		String location = resp.getLocationHeader();
		return location;
	}

	public OINKRequestMessage buildPostRequestMessage(Resource resource) {
		// Build OINKRequestMessage for Query
		OINKRequestMessage query = new OINKRequestMessage();
		String resourceName = resource.getResourceType().toString();
		query.setResourcePath("/" + resourceName);
		query.setMethod(HttpMethod.POST);
		query.setBody(new FhirBody(resource));
		return query;
	}

	public void setResolveCareProvider(boolean resolveCareProvider) {
		this.resolveCareProvider = resolveCareProvider;
	}

	public void setResolveManagingOrganization(boolean resolveManagingOrganization) {
		this.resolveManagingOrganization = resolveManagingOrganization;
	}

}
