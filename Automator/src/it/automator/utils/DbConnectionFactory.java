package it.automator.utils;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class DbConnectionFactory {
	
	/**
	 * 
	 * @author tony
	 *
	 */
	public class DbConnection {
		public DbConnection(String connUri, String db) {
			MongoClientURI connectionString = new MongoClientURI(connUri);
			mongoClient = new MongoClient(connectionString);
			database = mongoClient.getDatabase(db);
		}
		
		public MongoCollection<Document> getCollection(String collection) {
			if(collection == null || collection.trim().isEmpty()) return null;
			if(mongoClient == null) return null;
			
			boolean isDefined = false;
			MongoCursor<String> cursor = database.listCollectionNames().iterator();
			try {
				while(cursor.hasNext() && !isDefined) {
					isDefined = cursor.next().equals(collection);
				}
			}
			finally {
				cursor.close();
			}
			
			if(isDefined) {
				return database.getCollection(collection);
			}
			
			return null;
		}
		
		
		public void close() {
			if(mongoClient == null) return;
			mongoClient.close();
			database = null;
			mongoClient = null;
		}
		
		@Override
		public void finalize() throws Throwable {
			super.finalize();
			close();
		}
		
		private MongoClient mongoClient = null;
		private MongoDatabase database = null;
	}
	
	/* ---------------------------------------------------------- */
	
	
	public static DbConnectionFactory getInstance() {
		synchronized(DbConnectionFactory.class) {
			if(instance == null) {
				instance = new DbConnectionFactory();
			}
			return instance;
		}
	}
	
	public DbConnectionFactory setConnectionUri(String uri) {
		connectionURI = uri;
		return this;
	}
	
	public DbConnectionFactory setDatabase(String db) {
		dbName = db;
		return this;
	}
	
	public DbConnection getConnection() {
		return new DbConnection(connectionURI, dbName);
	}
	

	public DbConnectionFactory() {
		
	}
	
	private static DbConnectionFactory instance = null;
	private String connectionURI = null;
	private String dbName = null;
	
	
	

}
