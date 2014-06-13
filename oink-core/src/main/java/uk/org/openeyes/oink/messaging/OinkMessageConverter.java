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
package uk.org.openeyes.oink.messaging;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.org.openeyes.oink.domain.OINKMessage;
import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;
import uk.org.openeyes.oink.domain.json.OinkRequestMessageJsonConverter;
import uk.org.openeyes.oink.domain.json.OinkResponseMessageJsonConverter;

/**
 * A custom Camel {@link TypeConverter} for converting
 * {@link OINKMessage} objects to/from byte arrays and JSON strings
 * 
 * @author Oliver Wilkie
 */
@Converter
public class OinkMessageConverter {
	
	private static final Logger log = LoggerFactory.getLogger(OinkMessageConverter.class);

	OinkRequestMessageJsonConverter reqConv;
	OinkResponseMessageJsonConverter respConv;

	public OinkMessageConverter() {
		reqConv = new OinkRequestMessageJsonConverter();
		respConv = new OinkResponseMessageJsonConverter();
	}

	@Converter
	public byte[] toByteArray(OINKRequestMessage message) {
		String json = toJsonString(message);
		return SerializationUtils.serialize(json);
	}

	@Converter
	public OINKRequestMessage fromByteArray(byte[] message) {
		String json = (String) SerializationUtils.deserialize(message);
		return requestMessageFromJsonString(json);
	}

	@Converter
	public String toJsonString(OINKRequestMessage message) {
		return reqConv.toJsonString(message);
	}
	
	@Converter
	public OINKRequestMessage fromJsonString(String s) {
		return requestMessageFromJsonString(s);
	}
	
	@Converter
	public OINKResponseMessage responseFromJsonString(String s) {
		return responseMessageFromJsonString(s);
	}

	@Converter
	public OINKRequestMessage requestMessageFromJsonString(String message) {
		return reqConv.fromJsonString(message);
	}

	@Converter
	public byte[] toByteArray(OINKResponseMessage message) {
		String json = toJsonString(message);
		return SerializationUtils.serialize(json);
	}

	@Converter
	public OINKResponseMessage responseMessageFromByteArray(byte[] message) {
		String json = (String) SerializationUtils.deserialize(message);
		log.debug("Message is: "+json);
		return responseMessageFromJsonString(json);
	}

	@Converter
	public String toJsonString(OINKResponseMessage message) {
		return respConv.toJsonString(message);
	}

	@Converter
	public OINKResponseMessage responseMessageFromJsonString(String message) {
		return respConv.fromJsonString(message);
	}

}
