package it.automator.commands;

import java.util.HashMap;
import java.util.Map;

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

public final class CommandLoader {
	
	public static CommandLoader getInstance() {
		synchronized(CommandLoader.class) {
			if(instance == null) {
				instance = new CommandLoader();
			}
			return instance;
		}
	}
	
	public CommandLoader setDbConfig(String url, String name) throws JsonMapperException {
		if(dbUrl == null || dbName == null) {
			dbUrl = url;
			dbName = name;
			loadCommands();
		}
		return this;
	}

	
	private CommandLoader() {
		log = Logger.getLogger(CommandLoader.class);
		log.debug(String.format("CommandsLoader init..."));
		
		commandsMap = new HashMap<>();
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
			
			MongoCollection<Document> coll = conn.getCollection(CommandLoader.DB_COMMANDS_COLLECTION_NAME);
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
					    
					    CommandObject co = new CommandObject(commandId, commandName, commandVersion, commandClassname, commandLang, commandCode);
					    commandsMap.put(co.getCommandName(), co);
					    
					    log.debug(String.format("ID := %d, NAME := '%s', VERSION := '%s', CLASSNAME := '%s', LANG := '%s', CODE := '%s'", commandId, commandName, commandVersion, commandClassname, commandLang, commandCode));
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
	
	private Map<String, CommandObject> commandsMap = null; 

	private static CommandLoader instance = null;
	
	private static final String DB_COMMANDS_COLLECTION_NAME = "commands";
}
