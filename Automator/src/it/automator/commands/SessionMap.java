package it.automator.commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SessionMap {

	public SessionMap(String id) {
		log = Logger.getLogger(SessionMap.class);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmssSS");
		if(id==null || id.trim().equals("")) {
			sessionId = "UNNAMED_SESSION";
		}
		else {
			sessionId = id;
		}
		sessionId += "[" + sdf.format(new Date()) + "]";
		
		log.debug(String.format("SessionMap with id '%s' init...", sessionId));
		
		valuesMap = new HashMap<>();
		variablesMap = new HashMap<>();
	}
	
	
	public boolean containsValueKey(String k) {
		return valuesMap.containsKey(k);
	}
	
	public Object addValue(String k, Object v) {
		if(!containsValueKey(k)) {
			valuesMap.put(k, v);
			return v;
		}
		log.error(String.format("Values can only write once. Cannot be overwrite. Value '%s' already exists.", k));
		return null;
	}
	
	
	public boolean containsVariableKey(String k) {
		return variablesMap.containsKey(k);
	}
	
	public Object addVariable(String k, Object v) {
		if(containsVariableKey(k)) {
			log.error(String.format("Variable '%s' will be overwrite.", k));
		}
		variablesMap.put(k, v);
		return v;
	}
	
	public Object removeVariable(String k) {
		if(!containsVariableKey(k)) {
			log.error(String.format("Variable '%s' is not defined. Nothing to do.", k));
		}
		return variablesMap.remove(k);
	}
	
	
	public String toJson() {
		String format = "{"
				+ "\"values\" : %s,"
				+ "\"variables\" : %s" 
				+ "}";
		
		ObjectMapper mapper = new ObjectMapper();

		try {
			return String.format(format, mapper.writeValueAsString(valuesMap), mapper.writeValueAsString(variablesMap));
		}
		catch (JsonProcessingException e) {
			log.error(e);
			return null;
		}
	}
	
	
	private Logger log = null;
	
	private String sessionId = null;
	
	private Map<String, Object> valuesMap = null;
	private Map<String, Object> variablesMap = null;

}
