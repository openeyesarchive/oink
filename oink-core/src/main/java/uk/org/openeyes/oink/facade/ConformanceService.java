package uk.org.openeyes.oink.facade;

import org.hl7.fhir.instance.model.Conformance;

/**
 * 
 * Builds the FHIR Conformance Model for a Facade Route
 * 
 * TODO
 * 
 * @author Oliver Wilkie
 */
public class ConformanceService {

	private final RoutingService routingService;

	public ConformanceService(RoutingService service) {
		this.routingService = service;
	}

	public Conformance generateConformance() {

		Conformance c = new Conformance();
		return c;
	}

}
