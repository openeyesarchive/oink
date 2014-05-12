package uk.org.openeyes.oink.hl7v2.test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.primitive.CommonTS;
import ca.uhn.hl7v2.model.v24.datatype.TS;
import ca.uhn.hl7v2.model.v24.datatype.XCN;
import ca.uhn.hl7v2.model.v24.message.QRY_A19;
import ca.uhn.hl7v2.model.v24.segment.QRD;

public class Hl7MessageBuilderTest {
	
	@Test
	public void foo() throws HL7Exception, IOException {
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
		qrd.getQueryID().setValue("IDENTIFIER");
		// Set quantity limited request
		qrd.getQuantityLimitedRequest().getQuantity().setValue("10");
		qrd.getQuantityLimitedRequest().getUnits().getIdentifier().setValue("RD");
		// !! Set who subject filter
		XCN who0 = qrd.getWhoSubjectFilter(0);
		who0.getIDNumber().setValue("NHSNUMBER");
		who0.getAssigningAuthority().getUniversalID().setValue("NHS");
		// Set what subject filter
		qrd.getWhatSubjectFilter(0).getIdentifier().setValue("DEM");
		// Set what department data code SKIP?
		
		// QRF
		// None

	}

}
