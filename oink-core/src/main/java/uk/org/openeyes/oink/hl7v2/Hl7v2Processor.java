package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * 
 * Processes an incoming Hl7v2 message by converting it into a FHIR bundle using
 * an OpenMapsSW XSL transform and then processing that Bundle to send the
 * resources over OINK.
 * 
 */
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
		hl7v2ValidationContext = new DefaultValidationWithoutTN(); // default
																	// validation
																	// rules
																	// without
																	// rules for
																	// US phone
																	// numbers
		hl7v2Validator = new MessageValidator(hl7v2ValidationContext, true);
	}

	public void setXsltPath(org.springframework.core.io.Resource xslFile)
			throws IOException {
		if (!xslFile.exists()) {
			log.error("Xsl not found");
			throw new IllegalArgumentException("Resource not found "
					+ xslFile.getDescription());
		}
		this.resource = xslFile;
	}

	public void process(@Body Message message, Exchange ex) throws Exception {

		// Validate Hl7v2 message
		hl7v2Validator.validate(message);

		// Convert to HL7v2 XML format
		String hl7Xml = hl7v2Converter.toXml(message);
		
		if (!resource.exists()) {
			String s = "No XLS transform found for this converter";
			log.error(s);
			throw new OinkException(s);
		}
		
		// Map to FHIR XML format
		String fhirXml = XmlTransformer.transform(hl7Xml,
				resource.getInputStream());
		
		// Bug Fix -- Remove empty tags (valid transform shouldnt have them anyway)
		fhirXml = fhirXml.replaceAll("<[a-zA-Z0-9]*/>", "");

		// Convert to FHIR Bundle
		AtomFeed bundle = fhirConverter.fromXmlToBundle(fhirXml);

		// Process FHIR bundle entries
		processResourcesInBundle(bundle, ex);

		log.debug("Processed a message..DONE");
	}

	/**
	 * Takes a Bundle and processes its components as individual FHIR Rest
	 * Resources
	 */
	public abstract void processResourcesInBundle(AtomFeed bundle, Exchange ex)
			throws OinkException;

}
