package jsr223.postnashhorn;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

/**
 * Nashorn is supposed to be removed after JDK 11...
 *
 * https://docs.oracle.com/en/java/javase/11/scripting/java-scripting-api.html#GUID-C4A6EB7C-0AEA-45EC-8662-099BDEFC361A
 * https://golb.hplar.ch/2020/04/java-javascript-engine.html
 *
 * GraalVM sounds like a way to keep going
 *
 *     <dependency>
 *       <groupId>org.graalvm.js</groupId>
 *       <artifactId>js</artifactId>
 *       <version>21.1.0</version>
 *     </dependency>
 *     <dependency>
 *       <groupId>org.graalvm.js</groupId>
 *       <artifactId>js-scriptengine</artifactId>
 *       <version>21.1.0</version>
 *     </dependency>
 */
public class GraalVM {

    public static void main(String... args) {

        List<ScriptEngineFactory> factories = new ScriptEngineManager().getEngineFactories();

        factories.forEach(factory -> {
            System.out.println("Lang name  :" + factory.getLanguageName());
            System.out.println("Engine name:" + factory.getEngineName());
            System.out.println(factory.getNames().toString());
            System.out.println("-------------------------");
        });

        System.out.println("\nNow using GraalVM:");
        // Graal
        ScriptEngine graalEngine = new ScriptEngineManager().getEngineByName("graal.js");
        try {
            graalEngine.eval("print('Hello World!');");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}
