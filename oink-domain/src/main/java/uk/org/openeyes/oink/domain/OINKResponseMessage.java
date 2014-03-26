/*******************************************************************************
 * OINK - Copyright (c) 2014 OpenEyes Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uk.org.openeyes.oink.domain;

public class OINKResponseMessage extends OINKMessage {

	private int status; // Same as HTTP Codes
	private OINKBody body;

	public OINKResponseMessage() {
		this.body = new OINKBody();
	}

	public OINKResponseMessage(int status) {
		this.status = status;
		this.body = new OINKBody();
	}
	
	public OINKResponseMessage(int status,
			OINKBody body) {
		this.status = status;
		if (body != null) {
			this.body = body;
		} else {
			this.body = new OINKBody();
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
