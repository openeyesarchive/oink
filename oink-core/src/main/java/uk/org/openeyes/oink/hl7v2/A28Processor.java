package uk.org.openeyes.oink.hl7v2;

import org.hl7.fhir.instance.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.AcknowledgmentCode;
import ca.uhn.hl7v2.model.Message;
import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.HttpMethod;
import uk.org.openeyes.oink.domain.OINKRequestMessage;

/**
 * Takes an ADT-A01 Hl7v2 {@link Message} and produces a corresponding
 * {@link OINKRequestMessage} Patient POST
 * 
 * @author Oliver Wilkie
 */
public class A28Processor extends Hl7v2Processor {

	private static final Logger log = LoggerFactory
			.getLogger(A28Processor.class);

	@Override
	public OINKRequestMessage wrapResource(Resource r) {
		// Build OinkRequestMessage wrapping a FHIR Post
		OINKRequestMessage outMessage = new OINKRequestMessage();
		FhirBody body = new FhirBody(r);
		outMessage.setBody(body);
		outMessage.setMethod(HttpMethod.POST);
		outMessage.setResourcePath("/Patient");
		return outMessage;
	}	
	
}
