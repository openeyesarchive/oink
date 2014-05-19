package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.camel.Body;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
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
	
	private ResourceLoader resourceLoader;

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

	public OINKRequestMessage process(@Body Message message) throws Exception {
		log.debug("Processing a message");

		// Validate message
		hl7v2Validator.validate(message);
		
		// Convert to HL7v2 XML
		String hl7Xml = hl7v2Converter.toXml(message);

		// Map to FHIR XML
		String fhirXml = XmlTransformer.transform(hl7Xml, resource.getInputStream());

		// Convert to FHIR Resource
		AtomFeed bundle = fhirConverter.fromXmlToBundle(fhirXml);
		
		// Build FhirBody
		FhirBody body = buildFhirBody(bundle);
		

		OINKRequestMessage outMessage = new OINKRequestMessage();
		outMessage.setBody(body);
		
		setRestHeaders(outMessage);
		
		log.debug("Processed a message");
		return outMessage;
	}
	
	
	public abstract FhirBody buildFhirBody(AtomFeed f);
	
	public abstract void setRestHeaders(OINKRequestMessage r);

}
