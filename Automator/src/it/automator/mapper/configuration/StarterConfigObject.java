package it.automator.mapper.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StarterConfigObject {
	
	@JsonCreator
	public StarterConfigObject(@JsonProperty("db")DbConfigObject db, @JsonProperty("tmpdir")String tmpdir) {
		this.db = db;
		this.tmpdir = tmpdir;
	}
	
	public DbConfigObject getDb() {
		return db;
	}

	public String getTmpdir() {
		return tmpdir;
	}

	private DbConfigObject db = null;
	private String tmpdir = null;

}
