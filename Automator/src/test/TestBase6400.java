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
		
		byte[] encodedBytes = Base64.encodeBase64(data);
		System.out.println("encodedBytes " + new String(encodedBytes));
		byte[] decodedBytes = Base64.decodeBase64(encodedBytes);
		System.out.println("decodedBytes " + new String(decodedBytes));
	}

	public static void main(String[] args) throws IOException {
		new TestBase6400();
	}

}
