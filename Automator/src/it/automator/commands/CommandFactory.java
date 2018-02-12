package it.automator.commands;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

import it.automator.mapper.configuration.JsonMapperException;
import it.automator.utils.DbConnectionFactory;
import it.automator.utils.DbConnectionFactory.DbConnection;

public final class CommandFactory {
	
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
	
	
	class CO implements CommandDescriptor{
		
		public CO(Long commandId, String commandName, String commandVersion, String commandClassname, String commandLang, String commandCode) throws CommandException {
			
			log = Logger.getLogger(CO.class);
			log.debug("CommandObject init...");
			
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


		public Class<?> getClazz() throws CommandException {
			synchronized(CO.class) {
				if(commandClazz == null) {
					log.debug(String.format("Load class '%s' from source. Object id %d", commandClassname, commandId));
				
					JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
				    DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		
				    JavaFileObject file = new JavaSourceFromString(commandClassname, commandCode);
				    
				    List<String> optionList = new ArrayList<>();
				    optionList.add("-d");
				    optionList.add(tmpDir.toAbsolutePath().toString());
		
				    Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
				    CompilationTask task = compiler.getTask(null, null, diagnostics, optionList, null, compilationUnits);
		
				    boolean success = task.call();
				    
				    if(success) {
						try {
							URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { tmpDir.toUri().toURL() });
							commandClazz = Class.forName(commandClassname, true, classLoader);
							
							if(!commandClazz.getSuperclass().getName().equals(AbstractCommand.class.getName())) {
								ClassCastException e = new ClassCastException(String.format("Class '%s' not extends '%s' as required.", commandClassname, AbstractCommand.class.getName()));
								log.error(e);
					        	throw e;
					        }
						}
						catch (ClassNotFoundException | MalformedURLException | ClassCastException ex) {
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
			}
		    
		    return commandClazz;
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
	
	// --------------------------------------
	// --------------------------------------
	
	
	public static CommandFactory getInstance() {
		synchronized(CommandFactory.class) {
			if(instance == null) {
				instance = new CommandFactory();
			}
			return instance;
		}
	}
	
	
	
	public CommandFactory configure(String url, String name, String tmp) throws JsonMapperException {
		if(dbUrl == null || dbName == null || tmpDir == null) {
			File d = new File(tmp);
			if(d==null || !d.exists() || !d.isDirectory() || !d.canRead() || !d.canWrite()) {
				JsonMapperException e = new JsonMapperException(String.format("Invalid temporary directory '%s'", tmp));
				log.error(e);
				throw e;
			}
			tmpDir = Paths.get(d.toURI());
			dbUrl = url;
			dbName = name;
			loadCommands();
		}
		return this;
	}
	
	
	
	private CommandFactory() {
		log = Logger.getLogger(CommandFactory.class);
		log.debug(String.format("CommandsLoader init..."));
		
		commandsMap = new HashMap<>();
	}
	
	
	
	private CommandDescriptor getCommandObject(Long id) throws CommandException {
		if(!commandsMap.containsKey(id) || commandsMap.get(id)==null) {
			CommandException e = new CommandException(String.format("Command with ID := %d not found", id));
			log.error(e);
			throw e;
		}
		return commandsMap.get(id);
	}
	
	
	
	public AbstractCommand createCommand(Long id) throws CommandException {
		Class<?> cl = getCommandObject(id).getClazz();
		try {
			AbstractCommand o = (AbstractCommand)cl.newInstance();
			return o;
		}
		catch (InstantiationException | IllegalAccessException ex) {
			CommandException e = new CommandException(String.format("Unable to create object from class id '%s'", id), ex);
			log.error(e);
			throw e;
		}
	}
	
	
	
	private void loadCommands() throws JsonMapperException {
		DbConnection conn = null;	
		
		try {		
			conn = DbConnectionFactory
				.getInstance()
				.setConnectionUri(dbUrl)
				.setDatabase(dbName)
				.getConnection();
			
			if(conn == null) {
				JsonMapperException e = new JsonMapperException(String.format("Failed to connect to DB '%s' and database '%s'", dbUrl, dbName));
				log.error(e);
				throw e;
			}
			
			MongoCollection<Document> coll = conn.getCollection(CommandFactory.DB_COMMANDS_COLLECTION_NAME);
			FindIterable<Document> commands = coll.find(Filters.eq("enabled", true));
			
			MongoCursor<Document> commandsIterator = commands.iterator();
			while(commandsIterator.hasNext()) {
				try {
					Document command = commandsIterator.next();
					if(command != null) {
						Long commandId = command.getLong("id");
						String commandName = command.getString("name");
					    String commandVersion = command.getString("version");
					    String commandClassname = command.getString("classname");
					    String commandLang = command.getString("lang");
					    String commandCode = new String(Base64.decodeBase64(command.getString("code").getBytes()));
					    
					    CO co = new CO(commandId, commandName, commandVersion, commandClassname, commandLang, commandCode);
					    commandsMap.put(co.getCommandId(), co);
					    
					    //createCommand(co.getCommandId()).execute(null);
					    
					    log.debug(String.format("ID := %d, NAME := '%s', VERSION := '%s', CLASSNAME := '%s', LANG := '%s', CODE := '%s'", commandId, commandName, commandVersion, commandClassname, commandLang, ""));//commandCode));
					}
				}
				catch(Exception e) {
					log.error(e);
				}
			}
		}
		finally {
			log.debug("Finalize db connection.");
			if(conn != null)
				conn.close();
		}
	}
	
	
	private Logger log = null;
	
	private String dbUrl = null;
	private String dbName = null;
	private Path tmpDir = null;
	
	private Map<Long, CO> commandsMap = null; 

	private static CommandFactory instance = null;
	
	private static final String DB_COMMANDS_COLLECTION_NAME = "commands";
}
