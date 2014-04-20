package uk.org.openeyes.oink.http;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;

import uk.org.openeyes.oink.domain.FhirBody;

@Converter
public class FhirBodyConverter {

	@Converter(allowNull=true)
	public static FhirBody toFhirBody(InputStream jsonIs, Exchange exchange) throws Exception {
		
		if (!exchange.getIn().getHeader(Exchange.CONTENT_TYPE,String.class).equals("application/json+fhir")) {
			throw new InvalidFhirMessageException("Could not parse Fhir Body. Content-Type should be application/json+fhir");
		}
		
		if (jsonIs.available() <= 0) {
			return null;
		}

		JsonParser parser = new JsonParser();
		try {
			ResourceOrFeed res = parser.parseGeneral(jsonIs);
			if (res.getFeed() != null) {
				return new FhirBody(res.getFeed());
			} else {
				return new FhirBody(res.getResource());
			}
		} catch (Exception e) {
			throw new InvalidFhirMessageException("Could not parse Fhir Body. Details: "
					+ e.getMessage());
		}

	}
	
	@Converter
	public static String toString(FhirBody body, Exchange exchange) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JsonComposer composer = new JsonComposer();
		if (body.isResource()) {
			composer.compose(os, body.getResource(), false);
		} else {
			composer.compose(os, body.getBundle(), false);
		}
		return os.toString();
	}

}
