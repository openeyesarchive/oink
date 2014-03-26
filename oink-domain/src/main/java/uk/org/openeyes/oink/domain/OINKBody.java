/*******************************************************************************
 * Copyright (c) 2014 OpenEyes Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
