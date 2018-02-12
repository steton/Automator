package it.automator.commands;

public interface CommandDescriptor {
	
	public Long getCommandId();
	public String getCommandName();
	public String getCommandVersion();
	public String getCommandClassname();
	public String getCommandLang();
	public Class<?> getClazz() throws CommandException;

}
