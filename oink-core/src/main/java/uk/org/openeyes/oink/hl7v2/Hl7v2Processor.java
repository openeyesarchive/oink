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

	private String xsltPath;
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

	public void setXsltPath(String path) throws IOException {
		this.xsltPath = loadResourceAsString(path);
	}

	public static String loadResourceAsString(String path) throws IOException {
		InputStream is = A28Processor.class.getResourceAsStream(path);
		StringWriter writer = new StringWriter();
		IOUtils.copy(is, writer);
		return writer.toString();
	}

	public OINKRequestMessage process(@Body Message message) throws Exception {
		log.debug("Processing a message");

		// Validate message
		hl7v2Validator.validate(message);
		
		// Convert to HL7v2 XML
		String hl7Xml = hl7v2Converter.toXml(message);

		// Map to FHIR XML
		String fhirXml = XmlTransformer.transform(hl7Xml, xsltPath);

		// Convert to FHIR Resource
		AtomFeed bundle = fhirConverter.fromXmlToBundle(fhirXml);
		if (bundle.getEntryList().size() > 1) {
			log.warn("The bundle produced by HL7 to FHIR XSL transform contains more than one entry");
		}
		Resource r = bundle.getEntryList().get(0).getResource();

		OINKRequestMessage outMessage = wrapResource(r);
		
		log.debug("Processed a message");
		return outMessage;
	}
	
	public abstract OINKRequestMessage wrapResource(Resource r);

}
