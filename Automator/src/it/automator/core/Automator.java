package it.automator.core;


import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import it.automator.commands.CommandFactory;
import it.automator.main.Starter;
import it.automator.mapper.configuration.JsonMapperException;
import it.automator.mapper.configuration.StarterConfigObject;
import it.automator.utils.DbConnectionFactory;
import it.automator.utils.DbConnectionFactory.DbConnection;


public class Automator {
	
	public Automator(String name, File basedir, StarterConfigObject conf) throws Exception {
		log = Logger.getLogger(Automator.class);
		log.debug(String.format("Automator '%s' init...", name));
		
		automatorBaseDir = basedir;
		automatorName = name;
		config = conf;
		
		// do what needed
		InternalProcessRegistry<Automator> ir = InternalProcessRegistry.getInstance();
		ir.subscribeAgent(this);
		
		initAutomator();
	}
	
	private void initAutomator() throws Exception {
		log.debug("Configure service.");
		configureAutomator();
		
		log.debug("Configure commands loader.");
		configureCommandFactory();
		
		log.debug("Execute flow manager.");
		executeFlowManager();
		
		log.debug("Execute web services.");
		executeWebServices();
	}
	
	
	private void configureCommandFactory() throws JsonMapperException {
		CommandFactory
			.getInstance()
			.configure(config.getDb().getUrl(), config.getDb().getDb(), config.getTmpdir());
	}
	

	private void configureAutomator() throws JsonMapperException, FileNotFoundException {
		
		DbConnection conn = null;	
				
		try {		
			conn = DbConnectionFactory
				.getInstance()
				.setConnectionUri(config.getDb().getUrl())
				.setDatabase(config.getDb().getDb())
				.getConnection();
			
			if(conn == null) {
				JsonMapperException e = new JsonMapperException(String.format("Failed to connect to DB '%s' and database '%s'", config.getDb().getUrl(), config.getDb().getDb()));
				log.error(e);
				throw e;
			}
			
			MongoCollection<Document> coll = conn.getCollection(Automator.DB_CONFIG_COLLECTION_NAME);
			Document application = coll.find(Filters.eq("id", automatorName)).first();
			
			if(application == null) {
				JsonMapperException e = new JsonMapperException(String.format("Unable to find configuration object into db"));
				log.error(e);
				throw e;
			}
			
			Document webserver = application.get("webserver", Document.class);
			
			String host = webserver.getString("host");
			Integer port = webserver.getInteger("port");
			Document keystore = webserver.get("keystore", Document.class);
			String ksFile = keystore.getString("file");
			String ksPasswd = keystore.getString("passwd");
			Integer poolmaxthread = webserver.getInteger("poolmaxthread");
			
			if(host==null || host.equals(Automator.NONE_STRING)) {
				JsonMapperException e = new JsonMapperException("Missing required parameter 'host' in tag 'application.webserver'.");
				log.error(e);
				throw e;
			}
			
			try {
				if(InetAddress.getByName(host).isMulticastAddress()) {
					JsonMapperException e = new JsonMapperException(String.format("Bad host address '%s'", host));
					log.error(e);
					throw e;
				}
			}
			catch (UnknownHostException ex) {
				JsonMapperException e = new JsonMapperException(ex);
				log.error(e);
				throw e;
			}
			
			if(port==null || port < 1024) {
				JsonMapperException e = new JsonMapperException("Missing required parameter 'value' in tag 'application.webServerPort'.");
				log.error(e);
				throw e;
			}
			
			File keyStoreFile = new File(automatorBaseDir.getAbsolutePath() + File.separator + ksFile);
			if(keyStoreFile == null || !keyStoreFile.exists() || !keyStoreFile.isFile() || !keyStoreFile.canRead()) {
				JsonMapperException e = new JsonMapperException(String.format("FileStore '%s' does not exists or it is not an existing readable file.", automatorBaseDir.getAbsolutePath() + File.separator + ksFile));
				log.error(e);
				throw e;
			}
			
			if(ksPasswd==null || ksPasswd.equals(NONE_STRING)) {
				JsonMapperException e = new JsonMapperException("Missing required parameter 'passwd' in tag 'application.keystore'.");
				log.error(e);
				throw e;
			}
			
			File webBaseDir = new File(automatorBaseDir.getAbsolutePath() + File.separator + Starter.WEBCONTENT_DIR_RELATIVE_PATH);
	        if(!webBaseDir.exists()) {
	            throw new FileNotFoundException(webBaseDir.getAbsolutePath());
	        }
	        
	        serverPoolMaxThreads = poolmaxthread;
	        serverServiceHost = host;
	    	serverServicePort = port;
	    	serverBasedirPath = automatorBaseDir.getAbsolutePath();
	    	serverWebBasePath = webBaseDir.getAbsolutePath();
	    	
	    	serverKeyStoreFile = keyStoreFile.getAbsolutePath();
	    	serverKeyStorePassword = ksPasswd;
	    	serverTrustStoreFile = keyStoreFile.getAbsolutePath();
	    	serverTrustStorePassword = ksPasswd;
		}
		finally {
			log.debug("Finalize db connection.");
			if(conn != null)
				conn.close();
		}
    	
    	log.debug(String.format("serverPoolMaxThreads   : %s", serverPoolMaxThreads));
    	log.debug(String.format("serverServiceHost      : %s", serverServiceHost));
    	log.debug(String.format("serverServicePort      : %s", serverServicePort));
    	log.debug(String.format("serverBasedirPath      : %s", serverBasedirPath));
    	log.debug(String.format("serverWebBasePath      : %s", serverWebBasePath));
    	log.debug(String.format("serverKeyStoreFile     : %s", serverKeyStoreFile));
    	log.debug(String.format("serverTrustStoreFile   : %s", serverTrustStoreFile));
	}

	
	private void executeWebServices() throws Exception {
		WebserverManager
			.getInstance()
			.setServerServiceHost(serverServiceHost, serverServicePort)
			.setBaseDir(serverWebBasePath)
			.setKeystore(serverKeyStoreFile, serverKeyStorePassword)
			.setTruststore(serverTrustStoreFile, serverTrustStorePassword)
			.setServerPoolMaxThreads(serverPoolMaxThreads)
			.start();
	}
	
	
	private void executeFlowManager() throws ActivityFlowException {
		ActivityFlowManager
			.getInstance()
			.configureAutomatorId(automatorName)
			.configureDB(config.getDb().getUrl(), config.getDb().getDb())
			.start();
	}
	
	
	public String getAutomatorName() {
		return automatorName;
	}
	
	// ###################################################################
	// ###################################################################
	// ###################################################################
	// ###################################################################
	
	
	private Logger log = null;
	
	private File automatorBaseDir = null;
	private String automatorName = null;
	private StarterConfigObject config = null;

	private Integer serverPoolMaxThreads = null;
	private Integer serverServicePort = null;
	private String serverServiceHost = null;
	private String serverBasedirPath = null;
	
	private String serverWebBasePath = null;
	private String serverKeyStoreFile = null;
	private String serverKeyStorePassword = null;
	private String serverTrustStoreFile = null;
	private String serverTrustStorePassword = null;
	
	
	//private final String schedulerPolicy = "0/5 * * * * *";

	
	
	private static final String NONE_STRING = "_#NONE#_";
	
	private static final String DB_CONFIG_COLLECTION_NAME = "config";
	
}
