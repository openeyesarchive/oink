package uk.org.openeyes.oink.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.camel.Converter;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.Resource;

import uk.org.openeyes.oink.domain.FhirBody;

@Converter
public class FhirResourceConverter {

	@Converter
	public static String toJsonString(Resource body) throws Exception {
		JsonComposer composer = new JsonComposer();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		composer.compose(os, body, false);
		return os.toString("UTF-8");
	}
	
	@Converter
	public static Resource fromJsonString(String string) throws Exception {
		JsonParser parser = new JsonParser();
		ByteArrayInputStream is = new ByteArrayInputStream(string.getBytes());
		return parser.parse(is);
	}
	
}
