package it.automator.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import it.automator.commands.AbstractCommand;
import it.automator.commands.CommandDescriptor;
import it.automator.commands.CommandException;
import it.automator.commands.SessionMap;

public class ActivityFlowInstance {

	public ActivityFlowInstance(Integer flowid, String flowname, String description, String automator, String schedulation,	Integer timeout, SessionMap session, List<CommandDescriptor> nodes) {
		log = Logger.getLogger(ActivityFlowInstance.class);
		log.debug("ActivityFlowInstance init...");
		
		this.flowid = flowid;
		this.flowname = flowname;
		this.description = description;
		this.automator = automator;
		this.schedulation = schedulation;
		this.timeout = timeout;
		this.session = session;
		this.nodes = nodes;
		
		commands = new ArrayList<>();
	}
	
	
	public void configure() throws CommandException {
		log.debug("configure ActivityFlowInstance...");
		for(CommandDescriptor c : nodes) {
			AbstractCommand cd = c.createCommand();
			commands.add(cd);
		}
	}
	
	
	
	
	public Integer getFlowid() {
		return flowid;
	}


	public String getFlowname() {
		return flowname;
	}


	public String getDescription() {
		return description;
	}


	public String getAutomator() {
		return automator;
	}


	public String getSchedulation() {
		return schedulation;
	}


	public Integer getTimeout() {
		return timeout;
	}


	public SessionMap getSession() {
		return session;
	}


	public List<CommandDescriptor> getNodes() {
		return nodes;
	}


	public List<AbstractCommand> getCommands() {
		return commands;
	}





	private Logger log = null;
	
	private Integer flowid = null;
	private String flowname = null;
	private String description = null;
	private String automator = null;
	private String schedulation = null;
	private Integer timeout = null;
	
	private SessionMap session = null;
	private List<CommandDescriptor> nodes = null;
	private List<AbstractCommand> commands = null;

}
