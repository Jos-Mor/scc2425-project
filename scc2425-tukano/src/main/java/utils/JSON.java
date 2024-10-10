package main.java.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

final public class JSON {
	final static ObjectMapper mapper = new ObjectMapper();

	synchronized public static final String encode(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}

	synchronized public static final <T> T decode(String json, Class<T> classOf) {
		try {
			var res = mapper.readValue(json, classOf);
			return res;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}

	synchronized public static final <T> T decode(String json, TypeReference<T> typeOf) {
		try {
			var res = mapper.readValue(json, typeOf);
			return res;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
}