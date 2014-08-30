package uk.org.openeyes.oink.spring;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class converts a JSON string map into a java.util.Map object, to
 * allow maps to be set in configuration.
 * 
 * @author Mark Sinclair
 *
 */
public class JSONPropertyEditor extends PropertyEditorSupport {
	
	private final static Logger log = LoggerFactory
			.getLogger(JSONPropertyEditor.class);

	@Override
	public String getAsText() {
		ObjectMapper mapper = new ObjectMapper();		
		Object object = getValue();
		try {
			return mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public void setAsText(String text) {
		try {
			ObjectMapper mapper = new ObjectMapper();		
			Object map = mapper.readValue(text, Map.class);
			setValue(map);
		} catch (JsonParseException e) {
			log.error("Json parse error", e);
		} catch (JsonMappingException e) {
			log.error("Json parse error", e);
		} catch (IOException e) {
			log.error("Json parse error", e);
		}
	}
}
