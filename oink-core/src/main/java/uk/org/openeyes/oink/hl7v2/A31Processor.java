package uk.org.openeyes.oink.hl7v2;

import org.hl7.fhir.instance.model.Resource;

import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;

public class A31Processor extends Hl7v2Processor {

	@Override
	public OINKRequestMessage wrapResource(Resource r) {
		// TODO rewrite
		OINKRequestMessage outMessage = new OINKRequestMessage();
		FhirBody body = new FhirBody(r);
		outMessage.setBody(body);
		outMessage.setMethod(HttpMethod.POST);
		outMessage.setResourcePath("/Patient");
		return outMessage;
	}

}
