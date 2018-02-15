package it.automator.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import it.automator.commands.CommandDescriptor;
import it.automator.commands.CommandFactory;
import it.automator.commands.SessionMap;
import it.automator.commands.ValidationException;
import it.automator.utils.DbConnectionFactory;
import it.automator.utils.DbConnectionFactory.DbConnection;

public class ActivityFlowManager {
	
	public static ActivityFlowManager getInstance() {
		synchronized(ActivityFlowManager.class) {
			if(instance == null) {
				instance = new ActivityFlowManager();
			}
			return instance;
		}
	}
	
	
	public ActivityFlowManager configureDB(String uri, String name) {
		dbUri = uri;
		dbName = name;
		return this;
	}
	
	
	public ActivityFlowManager configureAutomatorId(String n) {
		automatorId = n;
		return this;
	}
	
	public void start() throws ActivityFlowException {
		if(dbUri == null || dbUri.trim().isEmpty()) {
			ActivityFlowException e = new ActivityFlowException("Null or invalid db URI.");
			log.error(e);
			throw e;
		}
		
		if(dbName == null || dbName.trim().isEmpty()) {
			ActivityFlowException e = new ActivityFlowException("Null or invalid db name.");
			log.error(e);
			throw e;
		}
		
		activitiesMap.clear();
		
		loadActivityFlows();
	}

	
	private ActivityFlowManager() {
		log = Logger.getLogger(ActivityFlowManager.class);
		log.debug("ActivityFlowManager init...");
		
		activitiesMap = new HashMap<>();
	}
	
	
	private void loadActivityFlows() throws ActivityFlowException {
		try {
			
			conn = DbConnectionFactory
					.getInstance()
					.setConnectionUri(dbUri)
					.setDatabase(dbName)
					.getConnection();
			
			if(conn == null) {
				ActivityFlowException e = new ActivityFlowException(String.format("Unable to create a db connection. Uri := '%s', Db := '%s'", dbUri, dbName));
				log.error(e);
				throw e;
			}
			
			MongoCollection<Document> coll = conn.getCollection(ActivityFlowManager.DB_FLOWS_COLLECTION_NAME);
			if(coll == null) {
				ActivityFlowException e = new ActivityFlowException(String.format("Unable to create '%s' db collection.", DB_FLOWS_COLLECTION_NAME));
				log.error(e);
				throw e;
			}
			
			log.debug(String.format("Select flow description of '%s' flow.", automatorId));
			FindIterable<Document> flows = coll.find(Filters.eq("automator", automatorId));
			try {
				for(Document d : flows) {
					//log.info(d.toJson());
					
					Integer flowid = d.getInteger("flowid");
					String flowname = d.getString("flowname");
					String description = d.getString("description");
					String automator = d.getString("automator");
					String schedulation = d.getString("schedulation");
					Integer timeout = d.getInteger("timeout");
					
					SessionMap session = new SessionMap(flowname);
					List<CommandDescriptor> nodes = new ArrayList<>();
				
					
					if(d.get("initvalues") instanceof Document) {
						Document ivals = d.get("initvalues", Document.class);
						for(String k : ivals.keySet()) {
							log.debug(String.format("Retreive value ['%s' => '%s'] and load in sessionMap.", k, ""+ivals.get(k)));
							session.addValue(k, ivals.get(k));
						}
					}
					else {
						ValidationException ex = new ValidationException(String.format("Type class exception for entry 'initvalues'. It must be a Map."));
						log.error(ex);
						throw ex;
					}
					
					
					if(d.get("initvariables") instanceof Document) {
						Document ivars = d.get("initvariables", Document.class);
						for(String k : ivars.keySet()) {
							log.debug(String.format("Retreive variable ['%s' => '%s'] and load in sessionMap.", k, ""+ivars.get(k)));
							session.addVariable(k, ivars.get(k));
						}
					}
					else {
						ValidationException ex = new ValidationException(String.format("Type class exception for entry 'initvalues'. It must be a Map."));
						log.error(ex);
						throw ex;
					}
					
					// Retreive Nodes
					if(d.get("nodes") instanceof List<?>) {
						List<?> nn = (List<?>)d.get("nodes", ArrayList.class);
						for(Object n : nn) {
							if(n instanceof Integer) {
								Long nodeId = Integer.toUnsignedLong((Integer)n);
								log.info(String.format("Instantiate new object with id '%d'.", nodeId));
								CommandDescriptor c = CommandFactory.getInstance().getCommandObject(nodeId);
								nodes.add(c);
							}
							else {
								ValidationException ex = new ValidationException(String.format("Type class exception for element in 'nodes'. It must be an Integer."));
								log.error(ex);
								throw ex;
							}
						}
					}
					else {
						ValidationException ex = new ValidationException(String.format("Type class exception for entry 'nodes'. It must be a List of Integer."));
						log.error(ex);
						throw ex;
					}
					
					// Flow Instance creation and registering into the internal map. The flow will be activate later.
					ActivityFlowInstance afi = new ActivityFlowInstance(flowid, flowname, description, automator, schedulation,	timeout, session, nodes);
					activitiesMap.put(flowid, afi);
			
				}	
			}
			catch(Exception e) {
				log.error(e);
			}
		}
		finally {
			log.debug("Close db connection.");
			if(conn != null)
				conn.close();
			conn = null;
		}
		
		
		
	}
	
	private static ActivityFlowManager instance = null;
	
	private Logger log = null;
	
	private String dbUri = null;
	private String dbName = null;
	private String automatorId = null;
	private DbConnection conn = null;
	
	private Map<Integer, ActivityFlowInstance> activitiesMap = null;
	
	
	private static final String DB_FLOWS_COLLECTION_NAME = "flows"; 

}
