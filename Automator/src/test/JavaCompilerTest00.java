package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class JavaCompilerTest00 {
	
	public class JavaCompilerException extends Exception {
		public JavaCompilerException(String string) {
			super(string);
		}

		private static final long serialVersionUID = 1L;
	}

	public JavaCompilerTest00() throws Exception {
		
		System.out.println(new File(".").getAbsolutePath());
		Path fileSource = Paths.get(new File("automators/AUTOMATOR01/etc/Test01.java").toURI());
		System.out.println(fileSource.toFile().getName().replaceAll("\\.java$", ""));
		runClass(compileSource(fileSource));
		
	}
	
	
	private Path compileSource(Path javaFile) throws JavaCompilerException, IOException {
		
		if(!javaFile.toFile().exists() || !javaFile.toFile().isFile() || !javaFile.toFile().canRead()) {
			FileNotFoundException e = new FileNotFoundException(String.format("File '%s' doesn't exests.", javaFile.toFile().getAbsolutePath()));
			throw e;
		}
		
		ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
				
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Integer res = compiler.run(null, null, errBaos, javaFile.toFile().getAbsolutePath());
        if(res==null || res != 0) {
        	JavaCompilerException e = new JavaCompilerException(errBaos.toString());
        	//System.err.println(e.getLocalizedMessage());
        	//System.err.println(errBaos.toString());
        	errBaos.close();
        	throw e;
        }
        
        return javaFile.getParent().resolve(javaFile.toFile().getName().replaceAll("\\.java$", ".class"));
    }
	
	private void runClass(Path javaClass) throws Exception {
		String className = javaClass.toFile().getName().replaceAll("\\.class$", "");
        URL classUrl = javaClass.getParent().toFile().toURI().toURL();
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classUrl});
        Class<?> clazz = Class.forName(className, true, classLoader);
        
        if(!clazz.getSuperclass().getName().equals(AbstractNode.class.getName())) {
        	Exception e = new Exception(String.format("Class '%s' not extends '%s' as required.", className, AbstractNode.class.getName()));
        	throw e;
        }
        
        
        AbstractNode obj = (AbstractNode)clazz.newInstance();
        obj.execute(null);
    }

	public static void main(String[] args) throws Exception {
		new JavaCompilerTest00();
	}

}
