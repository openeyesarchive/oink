package uk.org.openeyes.oink.hl7v2;

import org.apache.camel.Exchange;
import org.hl7.fhir.instance.model.AtomFeed;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.QBP_Q21;
import ca.uhn.hl7v2.model.v24.segment.QPD;
import ca.uhn.hl7v2.model.v24.segment.RCP;
import ca.uhn.hl7v2.util.Terser;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.exception.OinkException;

public class Q22K22Processor extends QueryResponseProcessor {
	
	private int nextQueryTagValue = 1;

	private int getNextQueryTagValue() {
		int curr = nextQueryTagValue;
		nextQueryTagValue = nextQueryTagValue++ % (10^5);
		return curr;
	}
	
	/**
	 * Builds Q22 Message from OINKRequestMessage contents
	 */
	@Override
	public Message buildQuery(OINKRequestMessage request) throws Exception {
		
		QBP_Q21 msg = new QBP_Q21();
		Terser terser = new Terser(msg);
		
		// MSH segment
		msg.initQuickstart("QBP", "Q22", "P");

		// QPD segment
		QPD qpd = msg.getQPD();
		qpd.getQpd1_MessageQueryName().getCe1_Identifier().setValue("Q22");
		qpd.getQpd1_MessageQueryName().getCe2_Text().setValue("Find Candidates");
		qpd.getQpd2_QueryTag().setValue(Integer.toString(getNextQueryTagValue()));
		
		terser.set("QPD-3(0)-1", "");
		
		if (isSearchByName(request)) {
			int repetition = 0;
			if (isSearchByFamilyName(request)) {
				String familyName = getQueryParameterValue(request, "family");
				terser.set("QPD-3("+repetition+")-1", "@PID.5.1.1");
				terser.set("QPD-3("+repetition+")-2", familyName+"*");				
				repetition++;
			}
			if (isSearchByGivenName(request)) {
				String givenName = getQueryParameterValue(request, "given");
				terser.set("QPD-3("+repetition+")-1", "@PID.5.1.2");
				terser.set("QPD-3("+repetition+")-2", givenName+"*");						
			}
		} else if (isSearchByNHSNumber(request)) {
			String nhsNumber = extractIdentifierValue(request);
			terser.set("QPD-3(0)-1", "@PID.3.1");
			terser.set("QPD-3(0)-2", nhsNumber);
			terser.set("QPD-3(1)-1", "@PID.3.4");
			terser.set("QPD-3(1)-2", "NHS");				
		} else if (isSearchByPASNumber(request)) {
			String pasNumber = extractIdentifierValue(request);
			terser.set("QPD-3(0)-1", "@PID.3.1");
			terser.set("QPD-3(0)-2", pasNumber);
			terser.set("QPD-3(1)-1", "@PID.3.4");
			terser.set("QPD-3(1)-2", "PAS");					
		} else {
			throw new OinkException("Only search by name, nhs number or PAS number is supported");
		}
		
		// RCP segment
		RCP rcp = msg.getRCP();
		rcp.getQueryPriority().setValue("I");
		rcp.getRcp2_QuantityLimitedRequest().getCq1_Quantity().setValue("10");
		rcp.getRcp2_QuantityLimitedRequest().getCq2_Units().getCe1_Identifier().setValue("RD");
		
		return msg;
	}

	@Override
	public void processResourcesInResponseBundle(AtomFeed bundle, Exchange ex)
			throws OinkException {
		// TODO Auto-generated method stub
		
	}
	

}
