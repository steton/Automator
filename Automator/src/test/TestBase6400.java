package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.binary.Base64;

public class TestBase6400 {
	
	

	public TestBase6400() throws IOException {
		
		Path fileSource = Paths.get(new File("automators/AUTOMATOR01/etc/Test01.java").toURI());
		byte[] data = Files.readAllBytes(fileSource);
		
		System.out.println(" ---------------------------------------------------- ");
		byte[] encodedBytes = Base64.encodeBase64(data);
		System.out.println("encodedBytes " + new String(encodedBytes));
		System.out.println(" ---------------------------------------------------- ");
		byte[] decodedBytes = Base64.decodeBase64(encodedBytes);
		System.out.println("decodedBytes " + new String(decodedBytes));
		System.out.println(" ---------------------------------------------------- ");
		System.out.println("URL := " + new File("").toURI().toURL().toString());
		
	}
	
	/*
	private Class<?> loadClassByCode(String clName, byte[] clCode) throws IOException {
        URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
        Class<?> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("defineClass", parameters);
            method.setAccessible(true);
            Class<?> res = (Class<?>)method.invoke(sysloader, new Object[]{clName, clCode, 0, clCode.length});
            return res;
        }
        catch (Throwable t) {
            throw new IOException("Error, could not add URL to system classloader");
        }
    }
    */

	public static void main(String[] args) throws IOException {
		new TestBase6400();
	}
	
	//private static final Class<?>[] parameters = new Class[]{String.class, byte[].class, int.class, int.class};

}
