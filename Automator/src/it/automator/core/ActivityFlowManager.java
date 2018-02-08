package it.automator.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

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
	
	
	public ActivityFlowManager setAutomatorId(String n) {
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
			for(Document d : flows) {
				log.info(d.toJson());
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
	
	private Map<String, ActivityFlowInstance> activitiesMap = null;
	
	
	private static final String DB_FLOWS_COLLECTION_NAME = "flows"; 

}
