package uk.org.openeyes.oink.hl7v2;

import java.util.List;

import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Resource;

import uk.org.openeyes.oink.domain.OINKRequestMessage;

public class OpenEyesADTProcessor extends ADTProcessor {
	
	String gpProfile = "http://openeyes.org.uk/fhir/1.7.0/profile/Practitioner/Gp";
	String practiceProfile = "http://openeyes.org.uk/fhir/1.7.0/profile/Organization/Practice";
	
	@Override
	public OINKRequestMessage buildSearchRequestMessage(Resource resource,
			List<Identifier> ids) {
		OINKRequestMessage query = super.buildSearchRequestMessage(resource, ids);
		
		if (resource.getResourceType().toString().equals("Organization")) {
			query.setParameters(query.getParameters().concat("&_profile="+practiceProfile));
		} else if (resource.getResourceType().toString().equals("Practitioner")) {
			query.setParameters(query.getParameters().concat("&_profile="+gpProfile));
		}
		
		return query;
	}
	
	@Override
	public OINKRequestMessage buildPostRequestMessage(Resource resource) {
		OINKRequestMessage query = super.buildPostRequestMessage(resource);
		
		if (resource.getResourceType().toString().equals("Organization")) {
			query.addProfile(practiceProfile);
		} else if (resource.getResourceType().toString().equals("Practitioner")) {
			query.addProfile(gpProfile);
		}
		
		return query;
	}

}
