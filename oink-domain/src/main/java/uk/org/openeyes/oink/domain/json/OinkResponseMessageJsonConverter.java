package uk.org.openeyes.oink.domain.json;

import uk.org.openeyes.oink.domain.OINKResponseMessage;


public class OinkResponseMessageJsonConverter extends OinkMessageJsonConverter {
	
	public String toJsonString(OINKResponseMessage message) {
		return gson.toJson(message);
	}
	
	public OINKResponseMessage fromJsonString(String s) {
		return gson.fromJson(s, OINKResponseMessage.class);
	}

}
