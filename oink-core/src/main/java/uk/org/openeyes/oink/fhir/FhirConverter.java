package uk.org.openeyes.oink.fhir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.Converter;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.AtomFeed;

@Converter
public class FhirConverter {

	private JsonParser jsonParser;
	private XmlParser xmlParser;

	public FhirConverter() {
		xmlParser = new XmlParser();
	}

	@Converter
	public AtomFeed fromJsonOrXml(String input) throws FhirConversionException {
		AtomFeed f = null;
		try {
			InputStream is = new ByteArrayInputStream(input.getBytes());
			f = xmlParser.parseGeneral(is).getFeed();
		} catch (Exception e) {
			InputStream is = new ByteArrayInputStream(input.getBytes());
			try {
				f = jsonParser.parseGeneral(is).getFeed();
			} catch (Exception e1) {
				throw new FhirConversionException();
			}
		}
		return f;
	}
	
	public AtomFeed fromXmlToBundle(String xml) throws Exception {
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		AtomFeed f = xmlParser.parseGeneral(is).getFeed();
		return f;
	}

}
