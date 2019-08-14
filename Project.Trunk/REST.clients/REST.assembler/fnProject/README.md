## fnProject (WIP)

The doc to get started is [here](https://fnproject.io/tutorials/JavaFDKIntroduction/)

```
 $ fn start
```

```
 $ fn list contexts
```

We need three features:
- read the photo cell to get the ambient light
- set the relay status
- get the relay status

```
 $ fn init --runtime java --trigger http ambientlight
```

Then you can 
```
 $ cd ambientlight
 $ mvn [-Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-proxy-hqdc.us.oracle.com -Dhttps.proxyPort=80] clean package
``` 
> Note: It seems that the step above requires Java 9.
If you have an error like
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.3:compile (default-compile) on project hello: Fatal error compiling: invalid target release: 11 -> [Help 1]
[ERROR] 
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR] 
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/MojoExecutionException
```
then modify your `pom.xml`, `source` and `target` elements:
```xml
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.3</version>
        <configuration>
            <source>9</source>
            <target>9</target>
        </configuration>
    </plugin>
```

Then create an app
```
 $ fn create app java-light
Successfully created app:  java-light 
 $ fn list apps
  ...
``` 

In the `ambientlight` folder:
```
 $ fn --verbose deploy --app java-light --local
Deploying ambientlight to app: java-light
Bumped to version 0.0.2
Building image fndemouser/ambientlight:0.0.2 
FN_REGISTRY:  fndemouser
Current Context:  default
Sending build context to Docker daemon   47.1kB
Step 1/11 : FROM fnproject/fn-java-fdk-build:jdk9-1.0.98 as build-stage
...
```

Run it:
```
 $ fn invoke java-light ambientlight
 Hello, world!
```
If you've reached this point, it means that your infrastructure is in place!

We can now customize it to fit our needs.

### Refactor the generated code
- Rename the file `ambientlight/src/main/java/com/example/fn/HelloFunction.java` to `ambientlight/src/main/java/rpi/sensors/LightSensor.java`
- Change the code it contains:
```java
package rpi.sensors;

public class LightSensor {

  public String handleRequest(String input) {
    String name = (input == null || input.isEmpty()) ? "world"  : input;

    return "Hello, " + name + "!";
  }

}
```
- Modify the `func.yaml` accordingly, from
```yaml
run_image: fnproject/fn-java-fdk:jre11-1.0.98
cmd: com.example.fn.HelloFunction::handleRequest
triggers:
```
to
```yaml
run_image: fnproject/fn-java-fdk:jre11-1.0.98
cmd: rpi.sensors.LightSensor::retrieveData
triggers:
```
- Redeploy to make sure all is right
```
 $ fn deploy --app java-light --local
```
- And re-run
```
 $ fn invoke java-light ambientlight
Hello, world!
```
Good.

Now let's change the code to manage JSON objects:
```java
package rpi.sensors;

public class LightSensor {

  public static class Input {
    public String name;
  }

  public static class Result {
    public String requester;
    public String dataType;
    public float value;
  }

  public Result retrieveData(Input input) {
    Result result = new Result();
    result.requester = String.format("%s", (input != null && input.name != null && !input.name.isEmpty() ? input.name : "Nobody"));
    result.dataType = "ambient-light";
    result.value = (float)(100 * Math.random());

    return result;
  }

}
```
You might want to add `<skipTests>true</skipTests>` in the `pom.xml`'s properties.
```
 $ fn build
 $ fn deploy --app java-light --local
 $ echo -n '{"name":"Oliv"}' | fn invoke java-light ambientlight
 {"requester":"Oliv","dataType":"ambient-light","value":45.967487}
```

Then invoke with `curl`
```
 $ curl -X POST -H "Content-Type: application/json" http://localhost:8080/t/java-light/ambientlight -d '{"name":"Oliv"}'
 {"requester":"Oliv","dataType":"ambient-light","value":89.65619}
```
