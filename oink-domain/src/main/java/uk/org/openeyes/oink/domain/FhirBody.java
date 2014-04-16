package uk.org.openeyes.oink.domain;

import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;

public class FhirBody {
	
	private final AtomFeed a;
	private final Resource r;
	
	public FhirBody(AtomFeed a) {
		this.a = a;
		this.r = null;
	}
	
	public FhirBody(Resource r) {
		this.a = null;
		this.r = r;
	}
	
	public boolean isBundle() {
		return a != null;
	}
	
	public boolean isResource() {
		return r != null;
	}
	
	public AtomFeed getBundle() {
		return a;
	}
	
	public Resource getResource() {
		return r;
	}

}
