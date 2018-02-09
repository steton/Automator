package it.automator.commands;

public class CommandObject {
	
	public CommandObject(Long commandId, String commandName, String commandVersion, String commandClassname,
			String commandLang, String commandCode) {
		super();
		this.commandId = commandId;
		this.commandName = commandName;
		this.commandVersion = commandVersion;
		this.commandClassname = commandClassname;
		this.commandLang = commandLang;
		this.commandCode = commandCode;
	}
	
	
	public Long getCommandId() {
		return commandId;
	}
	
	
	public String getCommandName() {
		return commandName;
	}
	
	
	public String getCommandVersion() {
		return commandVersion;
	}
	
	
	public String getCommandClassname() {
		return commandClassname;
	}
	
	
	public String getCommandLang() {
		return commandLang;
	}
	
	
	public String getCommandCode() {
		return commandCode;
	}



	private Long commandId = null;
	private String commandName = null;
	private String commandVersion = null;
	private String commandClassname = null;
	private String commandLang = null;
	private String commandCode = null;
	
	

}
