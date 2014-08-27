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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.exception.OinkException;
import uk.org.openeyes.oink.fhir.BundleParser;
import uk.org.openeyes.oink.fhir.ResourceConverter;
import uk.org.openeyes.oink.xml.XmlTransformer;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.segment.PID;
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

	private byte[] xsl;
	private Hl7v2XmlConverter hl7v2Converter;
	private XmlTransformer transformer;
	private BundleParser fhirConverter;
	private ValidationContext hl7v2ValidationContext;
	private MessageValidator hl7v2Validator;
	
	private Map<String,String> patientIdentifierMap;
	private Map<String,String> practitionerIdentifierMap;
	private Map<String,String> organizationIdentifierMap;
	
	private boolean fixZTags;

	public Hl7v2Processor() {
		hl7v2Converter = new Hl7v2XmlConverter();
		fhirConverter = new BundleParser();
		hl7v2ValidationContext = new DefaultValidationWithoutTN(); // default
																	// validation
																	// rules
																	// without
																	// rules for
																	// US phone
																	// numbers
		hl7v2Validator = new MessageValidator(hl7v2ValidationContext, true);
		transformer = new XmlTransformer();
	}

	public void setXsltPath(org.springframework.core.io.Resource xslFile)
			throws IOException {
		if (!xslFile.exists()) {
			log.error("Xsl not found");
			throw new IllegalArgumentException("Resource not found "
					+ xslFile.getDescription());
		}
		xsl = IOUtils.toByteArray(xslFile.getInputStream());
		if (xsl == null || xsl.length == 0) {
			log.error("Xsl not found");
			throw new IllegalArgumentException("Resource invalid "
					+ xslFile.getDescription());
		}
	}

	public void process(@Body Message message, Exchange ex) throws Exception {
		
		log.debug("Processing incoming HL7v2 message of type: "+message.getName());
		
		doProcess(message, ex, null);
	}

	public void doProcess(@Body Message message, Exchange ex, ProcessorContext processorContext) throws Exception {
		
		String fhirXml = preProcess(message);
		
		// Convert to FHIR Bundle
		log.debug("Converting FHIR XML to FHIR Bundle");
		AtomFeed bundle = fhirConverter.fromXmlToBundle(fhirXml);
		
		// Process FHIR bundle entries
		log.debug("Processing contents of newly created FHIR Bundle");
		processResourcesInBundle(bundle, ex, processorContext);
		
		if(log.isDebugEnabled()) {
			log.debug("FHIR bundle ================>");
			for(AtomEntry<? extends Resource> e : bundle.getEntryList()) {
				String json = ResourceConverter.toJsonString(e.getResource());
				log.debug("resource --------------->\n{}\n<--------------- resource", json);
			}
			log.debug("<================ FHIR bundle");
		}

		log.debug("Finished processing incoming HL7v2 message of type: "+message.getName());
	}

	public String preProcess(Message message) throws HL7Exception,
			OinkException, TransformerFactoryConfigurationError,
			TransformerException, UnsupportedEncodingException {
		fixZTags(message);
		
		// Validate Hl7v2 message
		hl7v2Validator.validate(message);
		
		// FIXME: workaround - add PID1.2 to PID1.3, so NHS number is in identifiers
		PID pid = (PID)message.get("PID");
		int rep = pid.getPid3_PatientIdentifierListReps();
		pid.getPid3_PatientIdentifierList(rep).getCx1_ID().setValue(pid.getPid2_PatientID().getCx1_ID().getValue());
		pid.getPid3_PatientIdentifierList(rep).getCx5_IdentifierTypeCode().setValue(pid.getPid2_PatientID().getCx5_IdentifierTypeCode().getValue());
		
		if(log.isDebugEnabled()) {
			log.debug("HL7v2 ================>\n{}\n<================", message.encode().replace("\r", "\n"));
		}

		// Convert to HL7v2 XML format
		log.debug("Converting incoming HL7v2 message to XML format");
		String hl7Xml = hl7v2Converter.toXml(message);

		if(log.isDebugEnabled()) {
			log.debug("HL7v2 ================>\n{}\n<================", hl7Xml);
		}
		
		if (hl7Xml == null) {
			String s = "Hl7 message could not be written in XML format (needed for conversion)";
			log.error(s);
			throw new OinkException(s);
		}		
		
		// FIXME: workaround - change CX.7 to lowercase
		hl7Xml = hl7Xml.replaceAll("\\>HOME\\<", "\\>home\\<");
		
		InputStream xslIs = new ByteArrayInputStream(xsl);

		// Map to FHIR XML format
		log.debug("Converting HL7v2 XML to FHIR XML");
		String fhirXml = transformer.transform(hl7Xml,
				xslIs);
		
		// FIXME: workaround - remove empty tags (valid transform shouldnt have them anyway)
		fhirXml = fhirXml.replaceAll("<[a-zA-Z0-9]*/>", "");
		return fhirXml;
	}

	private void fixZTags(Message message) {
		
		if(fixZTags) {
			
			// TODO: find a way to exclude Z-tags from HAPI parsing or to ignore
			//       them during validation

			// Fix invalid Z tags so that they are of the format ZXX
			try {
				String messageString = message.encode();

				Pattern pattern = Pattern.compile("\\rZ\\S+\\|");
				Matcher matcher = pattern.matcher(messageString);
	
				StringBuffer sb = new StringBuffer();
				int z = 0;
				while (matcher.find()) {
					z++;
					matcher.appendReplacement(sb,
							"\\rZ" + String.format("%02d", z) + "|");
				}
				matcher.appendTail(sb);
				messageString = sb.toString();
	
				messageString = messageString.replace("\n", "\r");
				
				message.parse(messageString);
				
			} catch (HL7Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Takes a Bundle and processes its components as individual FHIR Rest
	 * Resources
	 */
	public abstract void processResourcesInBundle(AtomFeed bundle, Exchange ex, ProcessorContext processorContext)
			throws OinkException;
	
	public boolean isFixZTags() {
		return fixZTags;
	}

	public void setFixZTags(boolean fixZTags) {
		this.fixZTags = fixZTags;
	}
	
	public void setPatientIdentifierMap(Map<String,String> patientIdentifierMap) {
		this.patientIdentifierMap = patientIdentifierMap;
	}

	public void setPractitionerIdentifierMap(
			Map<String,String> practitionerIdentifierMap) {
		this.practitionerIdentifierMap = practitionerIdentifierMap;
	}

	public void setOrganizationIdentifierMap(
			Map<String,String> organizationIdentifierMap) {
		this.organizationIdentifierMap = organizationIdentifierMap;
	}
	
	protected void remapPatientIdentifiers(List<Identifier> identifiers) {
		remapIdentifiers(patientIdentifierMap, identifiers);
	}
	
	protected void remapOrganizationIdentifiers(List<Identifier> identifiers) {
		remapIdentifiers(organizationIdentifierMap, identifiers);
	}
	
	protected void setDefaultOrganizationIdentifierSystemType(Identifier identifier) {
		setDefault(organizationIdentifierMap, identifier);
	}
	
	protected void remapPractitionerIdentifiers(List<Identifier> identifiers) {
		remapIdentifiers(practitionerIdentifierMap, identifiers);
	}
	
	protected void setDefaultPractitionerIdentifierSystemType(Identifier identifier) {
		setDefault(practitionerIdentifierMap, identifier);
	}
	
	private final String DEFAULT_KEY = "default";
	
	private void setDefault(Map<String,String> map, Identifier identifier) {
		if(map.containsKey(DEFAULT_KEY)) {
			identifier.setSystemSimple(map.get(DEFAULT_KEY));
		}
	}
	
	protected void remapIdentifiers(Map<String, String> map, List<Identifier> identifiers) {
		for (Identifier id : identifiers) {
			for(String key : map.keySet()) {
				if (id.getSystemSimple().trim().matches(key)) {
					id.setSystemSimple(map.get(key).trim());
				}
			}
		}
	}
}
