package uk.org.openeyes.oink.hl7v2;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.messaging.OinkMessageConverter;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.datatype.XCN;
import ca.uhn.hl7v2.model.v24.message.QRY_A19;
import ca.uhn.hl7v2.model.v24.segment.QRD;

public class A19Builder {

	private final static Logger log = LoggerFactory.getLogger(A19Builder.class);

	public Message buildQuery(OINKRequestMessage request) throws HL7Exception,
			IOException {
		// Validate OINKRequestMessage

		// Build Query
		QRY_A19 message = new QRY_A19();
		message.initQuickstart("QRY", "A19", "P");

		List<NameValuePair> params = URLEncodedUtils.parse(
				request.getParameters(), Charset.forName("UTF-8"));

		// Populate the MSH Segment

		// Populate the QRD Segment
		QRD qrd = message.getQRD();
		for (NameValuePair param : params) {
			if (param.getName().equals("identifier")) {
				String value = param.getValue();
				if (value.startsWith("NHS")) {
					String[] split = value.split("\\|");
					if (split.length == 2) {
						String nhsNumber = split[1];
						XCN person = qrd.insertQrd8_WhoSubjectFilter(0);
						person.getIDNumber().setValue(nhsNumber);
						person.getIdentifierTypeCode().setValue("NHS");
					}
				}
			}
			// if (param.getName().equals("_id")) {
			// XCN person = qrd.insertQrd8_WhoSubjectFilter(i);
			// person.getIDNumber().setValue(param.getValue());
			// person.getIdentifierTypeCode().setValue("NHS");
			// i++;
			// } else if (param.getName().equals("family")) {
			// XCN person = qrd.insertQrd8_WhoSubjectFilter(i);
			// person.getFamilyName().getFn1_Surname().setValue(param.getValue());
			// i++;
			// }
		}

		// person.getFamilyName().getSurname().setValue("Wilkie");

		// [Optional] Populate the QRF segment
		return message;
	}

	public OINKResponseMessage processResponse(Message message) {
		return null;
	}

}
