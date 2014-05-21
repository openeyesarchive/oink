package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Organization;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.exception.OinkException;
import uk.org.openeyes.oink.fhir.FhirConverter;
import uk.org.openeyes.oink.xml.XmlTransformer;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.validation.MessageValidator;
import ca.uhn.hl7v2.validation.ValidationContext;
import ca.uhn.hl7v2.validation.impl.DefaultValidationWithoutTN;

public abstract class Hl7v2Processor {
	
	private static final Logger log = LoggerFactory
			.getLogger(Hl7v2Processor.class);

	private org.springframework.core.io.Resource resource;
	private MessageConverter hl7v2Converter;
	private FhirConverter fhirConverter;
	private ValidationContext hl7v2ValidationContext;
	private MessageValidator hl7v2Validator;
	
	public Hl7v2Processor() {
		hl7v2Converter = new MessageConverter();
		fhirConverter = new FhirConverter();
		hl7v2ValidationContext = new DefaultValidationWithoutTN(); // default validation rules without rules for US phone numbers
		hl7v2Validator = new MessageValidator(hl7v2ValidationContext, true);
	}

	public void setXsltPath(org.springframework.core.io.Resource xslFile) throws IOException {
		if (!xslFile.exists()) {
			log.error("Xsl not found");
			throw new IllegalArgumentException("Resource not found "+xslFile.getDescription());
		}
		this.resource = xslFile;
	}

	public void process(@Body Message message, Exchange ex) throws Exception {

		// Validate message
		hl7v2Validator.validate(message);
		
		// Convert to HL7v2 XML
		String hl7Xml = hl7v2Converter.toXml(message);

		// Map to FHIR XML
		String fhirXml = XmlTransformer.transform(hl7Xml, resource.getInputStream());

		// Convert to FHIR Bundle
		AtomFeed bundle = fhirConverter.fromXmlToBundle(fhirXml);
		
		// Process FHIR bundle entries
		postResourcesInBundle(bundle, ex);
		
		log.debug("Processed a message..DONE");
	}
	
	/**
	 * Takes a Bundle and processes its components as individual FHIR Rest Resources
	 */
	public abstract void postResourcesInBundle(AtomFeed bundle, Exchange ex) throws OinkException;
	
	public String searchForResourceByIdentifiers(Resource resource, List<Identifier> ids, Exchange ex) {
		
		// Build OINKRequestMessage for Query
		OINKRequestMessage query = new OINKRequestMessage();
		String resourceName = resource.getResourceType().toString();
		query.setResourcePath("/"+resourceName);
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
		
		CamelContext ctx = ex.getContext();
		ProducerTemplate prod = ctx.createProducerTemplate();
		OINKResponseMessage resp = (OINKResponseMessage) prod.requestBody("direct:rabbit-rpc", query);

		int status = resp.getStatus();
		
		AtomFeed bundle = resp.getBody().getBundle();
		if (bundle.getTotalResults() == 0) {
			return null;
		} else if (bundle.getTotalResults() > 1) {
			// throw exceptions
		} else {
			AtomEntry<? extends Resource> entry = bundle.getEntryList().get(0);
			return entry.getId();
		}
		
		return null;
	}
	
	public String postResource(Resource resource, Exchange ex) {
		
		// Build OINKRequestMessage for Query
		OINKRequestMessage query = new OINKRequestMessage();
		String resourceName = resource.getResourceType().toString();
		query.setResourcePath("/"+resourceName);
		query.setMethod(HttpMethod.POST);
		
		CamelContext ctx = ex.getContext();
		ProducerTemplate prod = ctx.createProducerTemplate();
		OINKResponseMessage resp = (OINKResponseMessage) prod.requestBody("direct:rabbit-rpc", query);

		int status = resp.getStatus();
		
		String location = resp.getLocationHeader();
		return location;
	}	

}
