package uk.org.openeyes.oink.domain;

public class OINKResponseMessage extends OINKMessage {

	private int status; // Same as HTTP Codes
	private OINKBody body;

	public OINKResponseMessage() {
		body = new OINKBody();
	}

	public OINKResponseMessage(int status,
			OINKBody body) {
		this.status = status;
		if (body != null) {
			this.body = body;
		}
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public OINKBody getBody() {
		return body;
	}

	public void setBody(OINKBody body) {
		this.body = body;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + body.hashCode();
		result = prime * result + (status);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OINKResponseMessage other = (OINKResponseMessage) obj;
		if (!body.equals(other.body))
			return false;
		if (status != other.status)
			return false;
		return true;
	}
}
