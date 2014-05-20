package uk.org.openeyes.oink.domain.json;

import uk.org.openeyes.oink.domain.OINKRequestMessage;

public class OinkRequestMessageJsonConverter extends OinkMessageJsonConverter {
	
	public String toJsonString(OINKRequestMessage message) {
		return gson.toJson(message);
	}
	
	public OINKRequestMessage fromJsonString(String s) {
		return gson.fromJson(s, OINKRequestMessage.class);
	}

}
