package it.automator.mapper.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DbConfigObject {
	
	@JsonCreator
	public DbConfigObject(@JsonProperty("url")String url, @JsonProperty("db")String db, @JsonProperty("user")String user, @JsonProperty("passwd")String passwd) {
		this.url = url;
		this.db = db;
		this.user = user;
		this.passwd = passwd;
	}
	
	public String getUrl() {
		return url;
	}
	
	
	public String getDb() {
		return db;
	}
	

	public String getUser() {
		return user;
	}

	
	public String getPasswd() {
		return passwd;
	}
	

	private String url = null;
	private String db = null;
	private String user = null;
	private String passwd = null;
}
