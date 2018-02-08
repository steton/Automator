package test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class TestExecJavascript00 {

	public static void main(String[] args) throws ScriptException {
		ScriptEngineManager factory = new ScriptEngineManager();
	    ScriptEngine engine = factory.getEngineByName("JavaScript");
	    engine.eval(""
	    		+ "print(Math.sqrt(9));"
	    		+ "print(java.lang.System.currentTimeMillis());"
	    		+ "");
	}

}
