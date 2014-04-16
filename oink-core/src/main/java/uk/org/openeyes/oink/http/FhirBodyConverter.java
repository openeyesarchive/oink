package uk.org.openeyes.oink.http;

import java.io.InputStream;

import org.apache.camel.Converter;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;

import uk.org.openeyes.oink.domain.FhirBody;

@Converter
public class FhirBodyConverter {

	@Converter(allowNull=true)
	public static FhirBody toFhirBody(InputStream jsonIs) throws Exception {
		
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
			throw new InvalidFhirMessage("Could not parse Fhir Body. Details: "
					+ e.getMessage());
		}

	}

}
