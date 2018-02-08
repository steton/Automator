package it.automator.mapper.servlets;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ServletRequestObject {

	@JsonCreator
	public ServletRequestObject(@JsonProperty("type")String type, @JsonProperty("hostid")Integer hostid, @JsonProperty("hostname")String hostname, @JsonProperty("command")Integer command, @JsonProperty("args")List<Object> args) {
		super();
		this.type = type;
		this.hostid = hostid;
		this.hostname = hostname;
		this.command = command;
		this.args = args;
	}
	
	public String getType() {
		return type;
	}
	
	public Integer getHostid() {
		return hostid;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public Integer getCommand() {
		return command;
	}
	
	public List<Object> getArgs() {
		return args;
	}

	private String type = null;
	private Integer hostid = null;
	private String hostname = null;
	private Integer command = null;
	private List<Object> args = null;

}
