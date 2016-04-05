package jsr223;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class ScriptEngineFactories
{
  public static void main(String[] args)
  {
    List<ScriptEngineFactory> factories = new ScriptEngineManager().getEngineFactories();
    System.out.println("=======================");
    for (ScriptEngineFactory factory : factories)
    {
      System.out.println("Lang name:" + factory.getLanguageName());
      System.out.println("Engine name:" + factory.getEngineName());
      System.out.println(factory.getNames().toString());
    }
    System.out.println("=======================");
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");
    try
    {
      engine.eval("println 'Hello Groovy!'");
      System.out.println(">>> Executing src/GroovyApp");
      engine.eval(new FileReader("src/GroovyApp.groovy"));
    }
    catch (ScriptException se)
    {
      se.printStackTrace();
    }
    catch (FileNotFoundException fnfe)
    {
      fnfe.printStackTrace();
    }
    System.out.println("Bye.");
  }
}
