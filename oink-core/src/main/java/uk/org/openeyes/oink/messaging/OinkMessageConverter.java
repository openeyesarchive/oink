package uk.org.openeyes.oink.messaging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.SerializationUtils;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.formats.ParserBase.ResourceOrFeed;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import uk.org.openeyes.oink.domain.OINKRequestMessage;
import uk.org.openeyes.oink.domain.OINKResponseMessage;

/**
 * A custom Camel {@link TypeConverter} for converting
 * {@link OINKRequestMessage} and {@link OINKResponseMessage} into byte arrays
 * and strings.
 * 
 * @author Oliver Wilkie
 */
@Converter
public class OinkMessageConverter {

	private final Gson gson;

	public OinkMessageConverter() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(AtomFeed.class,
				new AtomFeedGsonAdapter());
		gsonBuilder.registerTypeAdapter(Resource.class,
				new ResourceGsonAdapter());
		gson = gsonBuilder.create();
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
		return gson.toJson(message);
	}

	@Converter
	public OINKRequestMessage requestMessageFromJsonString(String message) {
		return gson.fromJson(message, OINKRequestMessage.class);
	}

	@Converter
	public byte[] toByteArray(OINKResponseMessage message) {
		String json = toJsonString(message);
		return SerializationUtils.serialize(json);
	}

	@Converter
	public OINKResponseMessage responseMessageFromByteArray(byte[] message) {
		String json = (String) SerializationUtils.deserialize(message);
		return responseMessageFromJsonString(json);
	}

	@Converter
	public String toJsonString(OINKResponseMessage message) {
		return gson.toJson(message);
	}

	@Converter
	public OINKResponseMessage responseMessageFromJsonString(String message) {
		return gson.fromJson(message, OINKResponseMessage.class);
	}

	private class ResourceGsonAdapter implements JsonSerializer<Resource>,
			JsonDeserializer<Resource> {

		@Override
		public Resource deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {

			JsonParser parser = new JsonParser();
			String jsonString = json.toString();
			InputStream is = new ByteArrayInputStream(jsonString.getBytes());
			try {
				ResourceOrFeed resourceOrFeed = parser.parseGeneral(is);
				return resourceOrFeed.getResource();
			} catch (Exception e) {
				throw new JsonParseException("Invalid Resource structure: "
						+ e.getMessage());
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}

		@Override
		public JsonElement serialize(Resource src, Type typeOfSrc,
				JsonSerializationContext context) {
			JsonComposer composer = new JsonComposer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				composer.compose(os, src, false);
				String element = os.toString();
				JsonObject ob = new JsonObject();
				com.google.gson.JsonParser gsonParser = new com.google.gson.JsonParser();
				return gsonParser.parse(element);
			} catch (Exception e) {
				return null;
			}
		}

	}

	private class AtomFeedGsonAdapter implements JsonSerializer<AtomFeed>,
			JsonDeserializer<AtomFeed> {

		@Override
		public AtomFeed deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {

			JsonParser parser = new JsonParser();
			String jsonString = json.toString();
			InputStream is = new ByteArrayInputStream(jsonString.getBytes());
			try {
				ResourceOrFeed resourceOrFeed = parser.parseGeneral(is);
				return resourceOrFeed.getFeed();
			} catch (Exception e) {
				throw new JsonParseException("Invalid AtomFeed structure: "
						+ e.getMessage());
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}

		@Override
		public JsonElement serialize(AtomFeed src, Type typeOfSrc,
				JsonSerializationContext context) {
			JsonComposer composer = new JsonComposer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				composer.compose(os, src, false);
				String element = os.toString();
				JsonObject ob = new JsonObject();
				com.google.gson.JsonParser gsonParser = new com.google.gson.JsonParser();
				return gsonParser.parse(element);
			} catch (Exception e) {
				return null;
			}
		}

	}

}
