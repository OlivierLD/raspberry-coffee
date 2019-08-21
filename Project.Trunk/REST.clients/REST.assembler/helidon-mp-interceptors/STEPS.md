## Interceptors
> Interceptors _only_ work in a EE or MP context.

> Interceptors need to be declared in a `beans.xml` file.

We will use Helidon MP (Micro Profile) for this example.

Generate scaffolding:
```
mvn archetype:generate 
    -DinteractiveMode=false       
    -DarchetypeGroupId=io.helidon.archetypes       
    -DarchetypeArtifactId=helidon-quickstart-mp       
    -DarchetypeVersion=1.0.0       
    -DgroupId=oic.helidon.examples 
    -DartifactId=helidon-mp-interceptors 
    -Dpackage=rpi.interceptors 
    -Dhttp.proxyHost=www-proxy-hqdc.us.oracle.com 
    -Dhttp.proxyPort=80 
    -Dhttps.proxyHost=www-proxy-hqdc.us.oracle.com 
    -Dhttps.proxyPort=80
``` 

Add 
- `@SecurityCheck.java` to declare the Annotation.
- `SecurityCheckInterceptor.java`, the actual interceptor.

In the pom.xml, add in the `<dependencies>` section:
```
    <dependency>
        <groupId>javax.interceptor</groupId>
        <artifactId>javax.interceptor-api</artifactId>
        <version>1.2.2</version>
    </dependency>
```

Make sure it works:
```
 $ mvn clean package -Dmaven.test.skip=true
 $ java -jar target/helidon-mp-interceptors.jar
```

Annotate `GreetResource.java`, method `getMessage`, with `@SecurityCheck`:
```java
    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @SecurityCheck(permission = "SAY_HI")
    public JsonObject getMessage(@PathParam("name") String name) {

        System.out.println(String.format("In getMessage, name: %s", name));

        return createResponse(name);
    }
```
And re-build, re-run. Then
```
 $ curl http://localhost:8080/greet/Oliv
 => In rpi.annotations.SecurityCheckInterceptor
 In getMessage, name: Oliv
```
The call was intercepted. Cool!

## Next
In the interceptor code:
```
@Interceptor
@SecurityCheck(permission = "SAY_HI")  // Wildcard for parameter?
public class SecurityCheckInterceptor {
```
we would need a wildcard for the `permission` value...
