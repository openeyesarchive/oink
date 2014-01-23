package uk.org.openeyes.oink.domain;

public enum HTTPMethod {
	
	GET,
	POST,
	UPDATE,
	DELETE,
	ANY;
		
	public static HTTPMethod fromString(String text) {
		if (text == null || text.isEmpty()) {
			return ANY;
		}
		return HTTPMethod.valueOf(text.toUpperCase());
	}

}
