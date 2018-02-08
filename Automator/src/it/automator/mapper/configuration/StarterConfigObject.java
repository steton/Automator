package it.automator.mapper.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StarterConfigObject {
	
	@JsonCreator
	public StarterConfigObject(@JsonProperty("db")DbConfigObject db) {
		this.db = db;
	}
	
	public DbConfigObject getDb() {
		return db;
	}

	private DbConfigObject db = null;

}
