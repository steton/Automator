package test;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class TestMongoConnection {

	public static void main(String[] args) {
		MongoClientURI connectionString = new MongoClientURI("mongodb://ec2-34-245-8-142.eu-west-1.compute.amazonaws.com:27017");
		MongoClient mongoClient = new MongoClient(connectionString);
		MongoDatabase database = mongoClient.getDatabase("autoDB");
		MongoCollection<Document> collection = database.getCollection("config");
		
		System.out.println(collection.count());
		
		Document myDoc = collection.find().first();
		System.out.println(" +++++ -> " + myDoc.toJson());
		
		Integer counter = 0;
		MongoCursor<Document> cursor = collection.find().iterator();
		try {
		    while (cursor.hasNext()) {
		        System.out.printf("%5d %s\n", counter++, cursor.next().toJson());
		    }
		}
		finally {
		    cursor.close();
		}
		
		
		
		Block<Document> printBlock = new Block<Document>() {
		     @Override
		     public void apply(final Document document) {
		         System.out.printf("## %s\n", document.toJson());
		     }
		};

		collection.find(Filters.gt("sample_ts", System.currentTimeMillis() - 3 * 60 * 60 * 1000)).forEach(printBlock);
		
		mongoClient.close();

	}

}
