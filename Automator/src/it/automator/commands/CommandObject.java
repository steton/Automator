package it.automator.commands;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.apache.log4j.Logger;

public class CommandObject {
	
	// --------------------------------------
	// --------------------------------------
	
	class JavaSourceFromString extends SimpleJavaFileObject {
		final String code;

		JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}
	
	// --------------------------------------
	// --------------------------------------
	
	public CommandObject(Long commandId, String commandName, String commandVersion, String commandClassname, String commandLang, String commandCode) throws CommandException {
		
		log = Logger.getLogger(CommandObject.class);
		log.debug("CommandObject init...");
		
		this.commandId = commandId;
		this.commandName = commandName;
		this.commandVersion = commandVersion;
		this.commandClassname = commandClassname;
		this.commandLang = commandLang;
		this.commandCode = commandCode;
		
		loadClazz();
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

	public AbstractCommand getCommand() throws CommandException {
		if(commandClazz == null) {
			CommandException e = new CommandException(String.format("Undefined class %s for command id %d", getCommandClassname(), getCommandId()));
			log.error(e);
	    	throw e;
		}
		
		try {
			AbstractCommand obj = (AbstractCommand)commandClazz.newInstance();
			return obj;
		}
		catch(Exception ex) {
			CommandException e = new CommandException(String.format("Error loading object from class %s for command id %d", getCommandClassname(), getCommandId()), ex);
			log.error(e);
	    	throw e;
		}
	}
	
	
	private void loadClazz() throws CommandException {
		log.debug(String.format("Load class '%s' from source. Object id %d", getCommandClassname(), getCommandId()));
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

	    JavaFileObject file = new JavaSourceFromString(getCommandClassname(), getCommandCode());

	    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
	    CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);

	    boolean success = task.call();
	    
	    if(success) {
			try {
				URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { new File("").toURI().toURL() });
				commandClazz = Class.forName(getCommandClassname(), true, classLoader);
			}
			catch (ClassNotFoundException | MalformedURLException ex) {
				commandClazz = null;
				CommandException e = new CommandException(ex);
				log.error(e);
		    	throw e;
			}
	    }
	    else {
	    	commandClazz = null;
	    	StringBuffer sb = new StringBuffer();
	    	for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
	  	      sb.append(String.format("Compolation error [%s]", diagnostic.getKind()));
	  	      sb.append(String.format(" Source  -> [line: %d - col: %d] %s", diagnostic.getLineNumber(), diagnostic.getColumnNumber(), diagnostic.getSource()));
	  	      sb.append(String.format(" Message -> %s", diagnostic.getMessage(null)));
	  	      sb.append("\n");
	  	    }	
	    	CommandException e = new CommandException(sb.toString());
	    	log.error(e);
	    	throw e;
	    }
	}
	
	private Logger log = null;
	
	private Class<?> commandClazz = null;

	private Long commandId = null;
	private String commandName = null;
	private String commandVersion = null;
	private String commandClassname = null;
	private String commandLang = null;
	private String commandCode = null;

}
