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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.hl7.fhir.instance.formats.Composer;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.Parser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class OINKBody {
	
	@JsonSerialize(using = ResourceToStringSerializer.class )
	@JsonDeserialize(using = ResourceDeserializer.class)
	private Resource resource;
	
	@JsonSerialize(using = FeedToStringSerializer.class)
	@JsonDeserialize(using = FeedToStringDeserializer.class)
	private AtomFeed feed;
	
	public OINKBody() {
		
	}
	
	public OINKBody(ResourceOrFeed resourceOrFeed) {
		if (resourceOrFeed.getFeed() != null) {
			feed = resourceOrFeed.getFeed();
		} else if (resourceOrFeed.getResource() != null) {
			resource = resourceOrFeed.getResource();
		}
	}
	
	public OINKBody(Resource resource) {
		this.resource = resource;
	}
	
	public OINKBody(AtomFeed feed) {
		this.feed = feed;
	}
	
	public void setContentAsResource(Resource resource) {
		this.resource = resource;
		this.feed = null;
	}
	
	public void setContentAsFeed(AtomFeed feed) {
		this.feed = feed;
		this.resource = null;
	}
	
	public Resource getResource() {
		return resource;
	}
	
	public AtomFeed getFeed() {
		return feed;
	}
	 
	
	public static class ResourceToStringSerializer extends JsonSerializer<Resource> {
		
		Composer fhirComposer = new JsonComposer();
		
		@Override
		public void serialize(Resource resource, JsonGenerator generator,
				SerializerProvider arg2) throws IOException,
				JsonProcessingException {
			if (resource == null) {
				generator.writeNull();
			} else {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				try {
					fhirComposer.compose(os, resource, false);
				} catch (Exception e) {
					throw new JsonParseException(e.getMessage(), JsonLocation.NA);
				}
				generator.writeRawValue(os.toString());
			}
		}
	}

	public static class ResourceDeserializer extends JsonDeserializer<Resource> {
		
		Parser parser = new org.hl7.fhir.instance.formats.JsonParser();;

		@Override
		public Resource deserialize(JsonParser jParser, DeserializationContext arg1)
				throws IOException, JsonProcessingException {
			
			ObjectCodec oc = jParser.getCodec();
			JsonNode node = oc.readTree(jParser);
			String str = node.toString();
			InputStream is = new ByteArrayInputStream(str.getBytes());
			try {
				return parser.parse(is);
			} catch (Exception e) {
				throw new IOException();
			}
		}
		
	}
	
	public static class FeedToStringSerializer extends JsonSerializer<AtomFeed> {
		
		Composer fhirComposer = new JsonComposer();
		
		@Override
		public void serialize(AtomFeed resource, JsonGenerator generator,
				SerializerProvider arg2) throws IOException,
				JsonProcessingException {
			if (resource == null) {
				generator.writeNull();
			} else {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				try {
					fhirComposer.compose(os, resource, false);
				} catch (Exception e) {
					throw new JsonParseException(e.getMessage(), JsonLocation.NA);
				}
				generator.writeRawValue(os.toString());		// TODO Could be source of error
			}
		}
	}

	public static class FeedToStringDeserializer extends JsonDeserializer<AtomFeed> {
		
		Parser parser = new org.hl7.fhir.instance.formats.JsonParser();

		@Override
		public AtomFeed deserialize(JsonParser jParser, DeserializationContext arg1)
				throws IOException, JsonProcessingException {
			
			ObjectCodec oc = jParser.getCodec();
			JsonNode node = oc.readTree(jParser);
			String str = node.toString();
			InputStream is = new ByteArrayInputStream(str.getBytes("UTF-8"));
			try {
				ResourceOrFeed result = parser.parseGeneral(is);
				return result.getFeed();
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			} finally {
				is.close();
			}
		}
		
	}

}
