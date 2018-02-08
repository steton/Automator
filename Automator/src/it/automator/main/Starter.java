package it.automator.main;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.automator.core.Automator;
import it.automator.mapper.configuration.StarterConfigObject;



public class Starter {
	
	public Starter(String[] args) throws Exception {
		
		log = Logger.getLogger(Starter.class);
		log.debug("Starter activated...");
				
		Options options = new Options();
		options.addOption("h", "help", false, "Print this help");
		options.addOption("d", "basedir", true, "Base directory");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args);
		HelpFormatter formatter = new HelpFormatter();
		
		if(cmd.hasOption("h") || !cmd.hasOption("d")) {
			formatter.printHelp(Starter.class.getName(), options );
			System.exit(0);
		}
		
		String baseDir = cmd.getOptionValue("d");
		File baseDirFile = new File(baseDir);
		if(baseDirFile == null || !baseDirFile.exists() || !baseDirFile.isDirectory() || !baseDirFile.canRead()) {
			formatter.printHelp(Starter.class.getName(), options);
			System.err.printf("Basedir %s is not a valid readable directory.\n", baseDir);
			System.exit(0);
		}
		
		
		
		log.debug(String.format("Check lib drectory '%s'.", baseDirFile.getAbsolutePath() + Starter.LIB_DIR_RELATIVE_PATH));
		File libDirFile = new File(baseDirFile.getAbsolutePath() + Starter.LIB_DIR_RELATIVE_PATH);
		if(libDirFile == null || !libDirFile.exists() || !libDirFile.isDirectory() || !libDirFile.canRead()) {
			formatter.printHelp(Starter.class.getName(), options);
			System.err.printf("File %s is not a valid readable lib directory.\n", baseDirFile.getAbsolutePath() + Starter.LIB_DIR_RELATIVE_PATH);
			System.exit(0);
		}
		
		File[] jarList = libDirFile.listFiles(new FileFilter() {
			@Override
			public boolean accept(File arg0) {
				return arg0.isFile() && arg0.canRead() && arg0.getAbsolutePath().toUpperCase().endsWith("JAR");
			}
		});
		
		log.debug(String.format("Load %d jar library files.", jarList.length));
		for(File f : jarList) {
			addURL(f.toURI().toURL());
		}
		
		// -----------------------------------------------
		
		log.debug(String.format("Check automators directory '%s'", baseDirFile.getAbsolutePath() + Starter.AUTOMATORS_DIR_RELATIVE_PATH));
		File automatorsDirFile = new File(baseDirFile.getAbsolutePath() + Starter.AUTOMATORS_DIR_RELATIVE_PATH);
		if(automatorsDirFile == null || !automatorsDirFile.exists() || !automatorsDirFile.isDirectory() || !libDirFile.canRead()) {
			formatter.printHelp(Starter.class.getName(), options);
			System.err.printf("File %s is not a valid readable automators directory.\n", baseDirFile.getAbsolutePath() + Starter.AUTOMATORS_DIR_RELATIVE_PATH);
			System.exit(0);
		}
		
		log.debug(String.format("Check automators config file '%s'", baseDirFile.getAbsolutePath() + Starter.CONF_FILE_RELATIVE_PATH));
		File baseConfigFile = new File(baseDirFile.getAbsolutePath() + Starter.CONF_FILE_RELATIVE_PATH);
		if(baseConfigFile == null && baseConfigFile.exists() || !baseConfigFile.isFile() || !baseConfigFile.canRead()) {
			log.debug(String.format("Not a valid readable config file.", baseDirFile.getAbsolutePath() + Starter.CONF_FILE_RELATIVE_PATH));
			System.err.printf("Not a valid readable config file.", baseDirFile.getAbsolutePath() + Starter.CONF_FILE_RELATIVE_PATH);
			System.exit(0);
		}
		
		log.debug(String.format("Open configuration file '%s'", baseConfigFile.getAbsolutePath()));
		byte[] jsonConfigBytes = Files.readAllBytes(Paths.get(baseConfigFile.getAbsolutePath()));
		ObjectMapper mapper = new ObjectMapper();
		StarterConfigObject config = mapper.readValue(jsonConfigBytes, StarterConfigObject.class);
		
		
		
		File[] automatorsDirList = automatorsDirFile.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return (pathname.isDirectory() && pathname.canRead() && !pathname.getName().startsWith("_"));
			}
		});
		
		for(File a : automatorsDirList) {
			log.debug(String.format("Found automator directory '%s'", a.getAbsolutePath()));
			
			log.debug(String.format("Check automator '%s'", a.getName()));
			File etcDirFile = new File(a.getAbsolutePath() + Starter.ETC_DIR_RELATIVE_PATH);
			if(etcDirFile != null && etcDirFile.exists() && etcDirFile.isDirectory() && etcDirFile.canRead()) {
				File webContentDirFile = new File(a.getAbsolutePath() + Starter.WEBCONTENT_DIR_RELATIVE_PATH);
				if(webContentDirFile != null && webContentDirFile.exists() && webContentDirFile.isDirectory() && webContentDirFile.canRead()) {
					// --------------------------------------------
			        // -- Use reflection for future use of different services.
			        try {
			        	Automator.class.getConstructor(String.class, File.class, StarterConfigObject.class).newInstance(a.getName(), a, config);
					}
			        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException| NoSuchMethodException | SecurityException e) {
			        	e.printStackTrace();
						log.error(e);
					}
				}
				else {
					log.debug(String.format("Automator '%s' is not valid because '%s' is not a valid readable directory. Skip.", a.getName(), a.getAbsolutePath() + Starter.WEBCONTENT_DIR_RELATIVE_PATH));
				}
			}
			else {
				log.debug(String.format("Automator '%s' is not valid because '%s' is not a valid readable directory. Skip.", a.getName(), a.getAbsolutePath() + Starter.ETC_DIR_RELATIVE_PATH));
			}
			
		}
	}
	
	
	private void addURL(URL u) throws IOException {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{ u }); 
        }
        catch (Throwable t) {
            log.error(t);
            throw new IOException("Error, could not add URL to system classloader");
        }        
    }
	
	
	public static void main(String[] args) throws Exception {
		new Starter(args);
	}
	
	private Logger log = null;
	
	private static final Class<?>[] parameters = new Class[]{URL.class};
	
	
	private static final String LIB_DIR_RELATIVE_PATH = File.separator + "lib";
	private static final String AUTOMATORS_DIR_RELATIVE_PATH = File.separator + "automators";
	private static final String CONF_FILE_RELATIVE_PATH = AUTOMATORS_DIR_RELATIVE_PATH + File.separator + "config.json";
	
	public static final String ETC_DIR_RELATIVE_PATH = File.separator + "etc";
	public static final String WEBCONTENT_DIR_RELATIVE_PATH = File.separator + "WebContent";
}
