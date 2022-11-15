package jsr223;

import javax.script.*;
import java.io.*;
import java.util.List;

/**
 * Scripting from Java 101.
 * Groovy and JavaScript
 */
public class ScriptEngineFactories {
	public static void main(String... args) {
		System.out.println("Your Java version:" + System.getProperty("java.version"));
		String location = new File(".").getAbsolutePath();
		System.out.printf("Running from %s\n", location);

		// Quick test for Python
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine pythonEngine = manager.getEngineByName("python");
		if (pythonEngine == null) {
			System.out.println(">> NO python engine.");
		} else {
			System.out.println(">> There IS a python engine!");
		}

		List<ScriptEngineFactory> factories = new ScriptEngineManager().getEngineFactories();
		System.out.println("=== Engine Factories ===");
		for (ScriptEngineFactory factory : factories) {
			System.out.println("Lang name  :" + factory.getLanguageName());
			System.out.println("Engine name:" + factory.getEngineName());
			System.out.println(factory.getNames().toString());
		}
		System.out.println("=======================");
		System.out.println("Act 1: Groovy.");
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
//  Writer writer = new StringWriter();
//  engine.getContext().setWriter(writer);
		try {
			engine.eval("println 'Hello Groovy!'");
			System.out.println(">>> Executing src/main/groovy/mainBasic.groovy");
			engine.eval(new FileReader("./" + "src/main/groovy/mainBasic.groovy"));
			//   String output = writer.toString();
			//   System.out.println("Output:[" + output + "]");
		} catch (ScriptException se) {
			System.err.printf("From %s:\n", location);
			se.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			System.err.println("From " + new File(".").getAbsolutePath() + ":");
			fnfe.printStackTrace();
		}
		// Execute Groovy function on a groovy object
		try {
			// Act as mainBasic.groovy
			engine.eval(new FileReader("./" + "src/main/groovy/GroovyBasic.groovy"));
			engine.eval("app = new GroovyBasic()");
			Object app = engine.get("app");
			Invocable invocable = (Invocable) engine;
			System.out.println(">> Invoking hello() method on GroovyBasic object...");
			invocable.invokeMethod(app, "hello"); // No prm. prms would be the 3rd arg and after.
			System.out.println("=== Done ===");
		} catch (ScriptException | NoSuchMethodException se_nsme) {
			se_nsme.printStackTrace();
		} catch (FileNotFoundException fnfe) {
			System.err.println("From " + new File(".").getAbsolutePath() + ":");
			fnfe.printStackTrace();
		}

		System.out.println("Act 2: JavaScript.");
		engine = new ScriptEngineManager().getEngineByName("javascript");
		try {
			engine.eval("print('From JS: Hello Nashorn!');");
		} catch (ScriptException se) {
			se.printStackTrace();
		}

		System.out.println("========");
		try {
			ScriptEngineManager factory = new ScriptEngineManager();
			engine = factory.getEngineByName("nashorn");
			String jsCode =
							"var hi = function() {" + "\n" +
							"  var a = 'PROSPER'.toLowerCase();" + "\n" +
							"  middle();" + "\n" +
							"  print('Live long and ' + a)" + "\n" +
							"}; " + "\n" +
							"var middle = function() {" + "\n" +
							"  var b = 1; " + "\n" +
							"  for (var i=0, max = 5; i<max; i++) {" + "\n" +
							"    b++;" + "\n" +
							"  }" + "\n" +
							"  print('>> b = ' + b + '.');" + "\n" +
							"};" + "\n" +
							"hi();";
			System.out.printf("Executing:\n%s\n", jsCode);
			engine.eval(jsCode);
		} catch (ScriptException se) {
			se.printStackTrace();
		}

		System.out.println("========");
		try {
			ScriptEngineManager factory = new ScriptEngineManager();
			engine = factory.getEngineByName("nashorn");
			engine.eval("load(\"" + "src" + "/" + "main" + "/" + "js" + "/" + "test1.js" + "\");");
		} catch (Exception se) {
			se.printStackTrace();
		}

		System.out.println("Bye.");
	}
}
