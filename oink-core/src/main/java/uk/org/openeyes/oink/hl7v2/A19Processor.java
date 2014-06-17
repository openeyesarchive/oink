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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.GregorianCalendar;
import java.util.List;

import net.wimpi.telnetd.io.terminal.xterm;

import org.apache.camel.Exchange;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hl7.fhir.instance.model.AtomFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.common.RandomStringGenerator;
import uk.org.openeyes.oink.domain.FhirBody;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.exception.OinkException;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.datatype.TS;
import ca.uhn.hl7v2.model.v24.datatype.XCN;
import ca.uhn.hl7v2.model.v24.message.QRY_A19;
import ca.uhn.hl7v2.model.v24.segment.QRD;

public class A19Processor extends Hl7v2Processor {

	private final static Logger log = LoggerFactory.getLogger(A19Processor.class);
	
	private RandomStringGenerator queryIdGenerator = new RandomStringGenerator(8);

	public Message buildQuery(OINKRequestMessage request) throws HL7Exception,
			IOException, OinkException {
		// Validate OINKRequestMessage

		QRY_A19 msg = new QRY_A19();

		// MSH segment
		msg.initQuickstart("QRY", "A19", "P");

		// QRD segment
		QRD qrd = msg.getQRD();
		// Set query time
		TS ts = qrd.getQueryDateTime();
		ts.getTs1_TimeOfAnEvent().setValue(new GregorianCalendar());
		// Set query format code
		qrd.getQueryFormatCode().setValue("R");
		// Set query priority
		qrd.getQueryPriority().setValue("I");
		// Set query id
		String queryId = queryIdGenerator.nextString();
		qrd.getQueryID().setValue(queryId);
		// Set quantity limited request
		qrd.getQuantityLimitedRequest().getQuantity().setValue("10");
		qrd.getQuantityLimitedRequest().getUnits().getIdentifier()
				.setValue("RD");
		// !! Set who subject filter
		if (isSearchByIdentifierNumber(request)) {
			log.info("Building an A19 with search by identifier");
			String system = extractSystem(request);
			String value = extractIdentifierValue(request);
			XCN who0 = qrd.getWhoSubjectFilter(0);
			who0.getIDNumber().setValue(value);
			who0.getIdentifierTypeCode().setValue(system);
		} else if (isSearchByFamilyName(request)) {
			log.info("Building an A19 with search by family name");
			String familyName = getQueryParameterValue(request, "family");
			XCN who0 = qrd.getWhoSubjectFilter(0);
			who0.getFamilyName().getSurname().setValue(familyName);
		} else {
			throw new OinkException("Only search by NHS number or family name currently supported");
		}
		// Set what subject filter
		qrd.getWhatSubjectFilter(0).getIdentifier().setValue("DEM");
		// Set what department data code SKIP?

		// QRF
		// None

		return msg;
	}
	
	private String getQueryParameterValue(OINKRequestMessage request, String key) {
		List<NameValuePair> params = URLEncodedUtils.parse(
				request.getParameters(), Charset.forName("UTF-8"));
		for (NameValuePair param : params) {
			if (param.getName().equals(key)) {
				String value = param.getValue();
				return value;
			}
		}
		return null;
	}

	private boolean isSearchByIdentifierNumber(OINKRequestMessage request) {
		String idvalue = getQueryParameterValue(request, "identifier");
		return idvalue != null;
	}
	
	private String extractIdentifierValue(OINKRequestMessage request) {
		String value = getQueryParameterValue(request, "identifier");
		if (value == null) {
			return null;
		}
		String[] split = value.split("\\|");
		if (split.length == 2) {
			String system = split[1];
			return system;
		}
		return null;
		
	}		
	
	private String extractSystem(OINKRequestMessage request) {
		String value = getQueryParameterValue(request, "identifier");
		if (value == null) {
			return null;
		}
		String[] split = value.split("\\|");
		if (split.length == 2) {
			String system = split[0];
			return system;
		}
		return null;
		
	}	
	
	private boolean isSearchByNHSNumber(OINKRequestMessage request) {
		String idvalue = getQueryParameterValue(request, "identifier");
		if (idvalue != null) {
			return idvalue.startsWith("NHS");
		}
		return false;
	}
	
	private boolean isSearchByFamilyName(OINKRequestMessage request) {
		String idvalue = getQueryParameterValue(request, "family");
		return idvalue != null;	
	}
	
	private String extractNHSNumber(OINKRequestMessage request) {
		String value = getQueryParameterValue(request, "identifier");
		if (value == null) {
			return null;
		}
		String[] split = value.split("\\|");
		if (split.length == 2) {
			String nhsNumber = split[1];
			return nhsNumber;
		}
		return null;
		
	}

	@Override
	public void processResourcesInBundle(AtomFeed bundle, Exchange ex) {
		OINKResponseMessage resp = new OINKResponseMessage();
		resp.setStatus(200);
		resp.setBody(new FhirBody(bundle));
		ex.getIn().setBody(resp);	
		ex.getIn().setHeader("rabbitmq.ROUTING_KEY", ex.getIn().getHeader("rabbitmq.REPLY_TO"));
		ex.getIn().removeHeader("rabbitmq.REPLY_TO");
		log.debug("Sending response with routing key "+ex.getIn().getHeader("rabbitmq.ROUTING_KEY"));
	}

}
