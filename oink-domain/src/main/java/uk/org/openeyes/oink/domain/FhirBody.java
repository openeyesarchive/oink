package uk.org.openeyes.oink.domain;

import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;

public class FhirBody {
	
	private final AtomFeed bundle;
	private final Resource r;
	
	public FhirBody(AtomFeed a) {
		this.bundle = a;
		this.r = null;
	}
	
	public FhirBody(Resource r) {
		this.bundle = null;
		this.r = r;
	}
	
	public boolean isBundle() {
		return bundle != null;
	}
	
	public boolean isResource() {
		return r != null;
	}
	
	public AtomFeed getBundle() {
		return bundle;
	}
	
	public Resource getResource() {
		return r;
	}

}
