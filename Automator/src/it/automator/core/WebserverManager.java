package it.automator.core;

import java.io.File;
import java.lang.management.ManagementFactory;

import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class WebserverManager {
	
	public static WebserverManager getInstance() {
		synchronized(WebserverManager.class) {
			if(instance == null) {
				instance = new WebserverManager();
			}
			return instance;
		}
	}
	
	public WebserverManager setServerPoolMaxThreads(Integer i) {
		serverPoolMaxThreads = i;
		return this;
	}
	
	public WebserverManager setServerServiceHost(String h, Integer p) {
		serverServiceHost = h;
		serverServicePort = p;
		return this;
	}
	
	public WebserverManager setKeystore(String f, String p) {
		serverKeyStoreFile = f;
        serverKeyStorePassword = p;
        return this;
	}
	
	public WebserverManager setTruststore(String f, String p) {
		serverTrustStoreFile = f;
        serverTrustStorePassword = p;
        return this;
	}
	
	public WebserverManager setBaseDir(String d) {
		serverWebBasePath = d;
		return this;
	}
	
	public void start() throws WebserverException {
		init();
		try {
			server.start();
		}
		catch (Exception e1) {
			WebserverException e = new WebserverException(e1);
			log.error(e);
			throw e;
		}
	}

	private WebserverManager() {
		log = Logger.getLogger(WebserverManager.class);
		log.debug(String.format("WebserverManager init..."));
	}
	
	private void init() throws WebserverException {
		
		if(serverPoolMaxThreads == null || serverPoolMaxThreads < 1) {
			WebserverException e = new WebserverException("Undefined value of serverPoolMaxThreads");
			log.error(e);
			throw e;
		}
		
		if(serverServicePort == null || serverServicePort < 1024) {
			WebserverException e = new WebserverException("Undefined value of serverServicePort");
			log.error(e);
			throw e;
		}
		
		if(serverServiceHost == null || serverServiceHost.trim().isEmpty()) {
			WebserverException e = new WebserverException("Undefined value of serverServiceHost");
			log.error(e);
			throw e;
		}
		
		if(serverKeyStoreFile == null || serverKeyStoreFile.trim().isEmpty()) {
			WebserverException e = new WebserverException("Undefined value of serverKeyStoreFile");
			log.error(e);
			throw e;
		}
		
		if(serverKeyStorePassword == null || serverKeyStorePassword.trim().isEmpty()) {
			WebserverException e = new WebserverException("Undefined value of serverKeyStorePassword");
			log.error(e);
			throw e;
		}
		
		if(serverTrustStoreFile == null || serverTrustStoreFile.trim().isEmpty()) {
			WebserverException e = new WebserverException("Undefined value of serverTrustStoreFile");
			log.error(e);
			throw e;
		}
		
		if(serverTrustStorePassword == null || serverTrustStorePassword.trim().isEmpty()) {
			WebserverException e = new WebserverException("Undefined value of serverTrustStorePassword");
			log.error(e);
			throw e;
		}
		
		if(serverWebBasePath == null || serverWebBasePath.trim().isEmpty()) {
			WebserverException e = new WebserverException("Undefined value of serverWebBasePath");
			log.error(e);
			throw e;
		}
		
		File serverWebBaseFile = new File(serverWebBasePath);
		if(serverWebBaseFile == null || !serverWebBaseFile.exists() || !serverWebBaseFile.isDirectory() || !serverWebBaseFile.canRead()) {
			WebserverException e = new WebserverException(String.format("Directory '%s' is not defined or not accessible.", serverWebBasePath));
			log.error(e);
			throw e;
		}
		
		File serverWebDescriptorFile = new File(serverWebBasePath + WebserverManager.DESCRIPTOR_FILE_RELATIVE_PATH);
		if(serverWebDescriptorFile == null || !serverWebDescriptorFile.exists() || !serverWebDescriptorFile.isFile() || !serverWebDescriptorFile.canRead()) {
			WebserverException e = new WebserverException(String.format("Descriptor file '%s' is not defined or not accessible.", serverWebBasePath + WebserverManager.DESCRIPTOR_FILE_RELATIVE_PATH));
			log.error(e);
			throw e;
		}
		
		
		
		
		
		
		QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(serverPoolMaxThreads);
 
        // Server
        server = new Server(threadPool);
        server.addBean(new ScheduledExecutorScheduler());
        
        MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        server.addBean(mbContainer);
        
        // Extra options
        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(false);
        server.setStopAtShutdown(true);
        
     // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(serverServicePort);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
        
        // -------------------------
 
        // === jetty-https.xml ===
        // SSL Context Factory
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(serverKeyStoreFile);
        sslContextFactory.setKeyStorePassword(serverKeyStorePassword);
        sslContextFactory.setKeyManagerPassword(serverKeyStorePassword);
        sslContextFactory.setTrustStorePath(serverTrustStoreFile);
        sslContextFactory.setTrustStorePassword(serverTrustStorePassword);
        sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
 
        // SSL HTTP Configuration
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());
 
        // SSL Connector
        ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(https_config));
        sslConnector.setPort(serverServicePort);
        sslConnector.setHost(serverServiceHost);
        server.addConnector(sslConnector);
        
        // === jetty-requestlog.xml ===
        NCSARequestLog requestLog = new NCSARequestLog();
        requestLog.setFilename(System.getProperty("ews.home") + File.separator + "log" + File.separator + "yyyy_mm_dd.request.log");
        requestLog.setFilenameDateFormat("yyyy_MM_dd");
        requestLog.setRetainDays(90);
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogCookies(true);
        requestLog.setLogTimeZone("GMT");
        requestLog.setLogServer(true);
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);

        ResourceConfig commonServletConfig = new ResourceConfig();
        commonServletConfig.packages(WebserverManager.INITIATOR_SERVLETS_PACKAGE);
        commonServletConfig.register(JacksonFeature.class);
    	ServletHolder commonServlet = new ServletHolder(new ServletContainer(commonServletConfig));

    	ServletContextHandler servletContext = new ServletContextHandler(server, "/servlet/*");
    	servletContext.setContextPath("/servlet/*");
    	servletContext.addServlet(commonServlet, "/*");
    	
    	WebAppContext webContext = new WebAppContext();
        webContext.setContextPath("/*");
        webContext.setDescriptor(serverWebBasePath + WebserverManager.DESCRIPTOR_FILE_RELATIVE_PATH);
        webContext.setResourceBase(serverWebBasePath);
        webContext.addAliasCheck(new AllowSymLinkAliasChecker());
    	
    	ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { webContext, servletContext });
 
        server.setHandler(contexts);
	}
	
	private Logger log = null;
	
	private Server server = null;
	
	private Integer serverPoolMaxThreads = null;
	private Integer serverServicePort = null;
	private String serverServiceHost = null;
	private String serverWebBasePath = null;
	
	private String serverKeyStoreFile = null;
	private String serverKeyStorePassword = null;
	private String serverTrustStoreFile = null;
	private String serverTrustStorePassword = null;
	
	private static WebserverManager instance = null;

	private static final String INITIATOR_SERVLETS_PACKAGE = "it.automator.servlets";
	private static final String DESCRIPTOR_FILE_RELATIVE_PATH = File.separator + "WEB-INF" + File.separator + "web.xml";
}
