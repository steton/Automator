package test;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.codehaus.commons.compiler.CompilerFactoryFactory;
import org.codehaus.commons.compiler.IClassBodyEvaluator;

public class JaninoTest00 {
	
	public class JaninoException extends Exception {

		private static final long serialVersionUID = 1L;

		@Override
		public String getMessage() {
			// TODO Auto-generated method stub
			return super.getMessage();
		}

		@Override
		public String getLocalizedMessage() {
			// TODO Auto-generated method stub
			return super.getLocalizedMessage();
		}

		@Override
		public synchronized Throwable getCause() {
			// TODO Auto-generated method stub
			return super.getCause();
		}

		@Override
		public synchronized Throwable initCause(Throwable cause) {
			// TODO Auto-generated method stub
			return super.initCause(cause);
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString();
		}

		@Override
		public void printStackTrace() {
			// TODO Auto-generated method stub
			super.printStackTrace();
		}

		@Override
		public void printStackTrace(PrintStream s) {
			// TODO Auto-generated method stub
			super.printStackTrace(s);
		}

		@Override
		public void printStackTrace(PrintWriter s) {
			// TODO Auto-generated method stub
			super.printStackTrace(s);
		}

		@Override
		public synchronized Throwable fillInStackTrace() {
			// TODO Auto-generated method stub
			return super.fillInStackTrace();
		}

		@Override
		public StackTraceElement[] getStackTrace() {
			// TODO Auto-generated method stub
			return super.getStackTrace();
		}

		@Override
		public void setStackTrace(StackTraceElement[] stackTrace) {
			// TODO Auto-generated method stub
			super.setStackTrace(stackTrace);
		}
		
		
		
	}

	public JaninoTest00() throws Exception {
		
		FileReader script = new FileReader(new File("/home/tony/Documents/DEV/eclipse-workspace/Automator/automators/AUTOMATOR01/etc/Test01.java"));
		
		
		IClassBodyEvaluator cbe = CompilerFactoryFactory.getDefaultCompilerFactory().newClassBodyEvaluator();
		cbe.setClassName("Test01");
        cbe.cook(script);
        Class<?> c = cbe.getClazz();
        
        System.out.println(c.getName());
        
        
        AbstractNode n = (AbstractNode)c.newInstance();
        System.out.println("RES := " + n.execute(null));
        
		
        /*
		Class<?> returnType = Integer.class;
        String[] parameterNames = {"pippo"};
        Class<?>[] parameterTypes = {String.class};
        Class<?>[] thrownExceptions = {JaninoException.class};
        String[] optionalDefaultImports = null;
        Object[] arguments = {"pluto"};
		
		// Create "ScriptEvaluator" object.
        IScriptEvaluator se = CompilerFactoryFactory.getDefaultCompilerFactory().newScriptEvaluator();
        se.setReturnType(returnType);
        se.setDefaultImports(optionalDefaultImports);
        se.setParameters(parameterNames, parameterTypes);
        se.setThrownExceptions(thrownExceptions);
        se.cook(script);
        
        */


	}

	public static void main(String[] args) throws Exception {
		new JaninoTest00();
	}

}
