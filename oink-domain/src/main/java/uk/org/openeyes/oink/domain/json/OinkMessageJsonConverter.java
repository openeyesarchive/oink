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
package uk.org.openeyes.oink.domain.json;

import java.lang.reflect.Type;

import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OinkMessageJsonConverter {

	protected final Gson gson;

	public OinkMessageJsonConverter() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(AtomFeed.class,
				new AtomFeedGsonAdapter());
		gsonBuilder.registerTypeAdapter(Resource.class,
				new ResourceGsonAdapter());
		gsonBuilder.registerTypeAdapter(String.class, new StringConverter());
		gson = gsonBuilder.create();
	}

	public class StringConverter implements JsonSerializer<String>,
			JsonDeserializer<String> {
		public JsonElement serialize(String src, Type typeOfSrc,
				JsonSerializationContext context) {
			if (src == null) {
				return new JsonPrimitive("");
			} else {
				return new JsonPrimitive(src.toString());
			}
		}

		public String deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			return json.getAsJsonPrimitive().getAsString();
		}
	}

}
