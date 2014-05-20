package uk.org.openeyes.oink.domain.json;

import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class OinkMessageJsonConverter {

	protected final Gson gson;

	public OinkMessageJsonConverter() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(AtomFeed.class,
				new AtomFeedGsonAdapter());
		gsonBuilder.registerTypeAdapter(Resource.class,
				new ResourceGsonAdapter());
		gson = gsonBuilder.create();
	}
	
}
