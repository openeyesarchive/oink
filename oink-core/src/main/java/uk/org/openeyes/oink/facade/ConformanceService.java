package uk.org.openeyes.oink.facade;

import org.hl7.fhir.instance.model.Conformance;

/**
 * 
 * @author Oliver Wilkie
 *
 */
public class ConformanceService {
	
	public Conformance generateConformance() {
		
		Conformance c = new Conformance();
		return c;
	}

}
