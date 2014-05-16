package uk.org.openeyes.oink.hl7v2;

import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;

public class A01Processor extends Hl7v2Processor {
	
	private Logger log = LoggerFactory.getLogger(A01Processor.class);

	@Override
	public FhirBody convertToFhirBody(AtomFeed f) {
		if (f.getEntryList().size() > 1) {
			log.warn("The bundle produced by HL7 to FHIR XSL transform contains more than one entry");
		}
		Resource r = f.getEntryList().get(0).getResource();
		FhirBody b = new FhirBody(r);
		return b;
	}

	@Override
	public void setRestHeaders(OINKRequestMessage r) {
		r.setResourcePath("/Patient");		
		r.setMethod(HttpMethod.POST);
	}

}
