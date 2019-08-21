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
```java
@Inherited
@InterceptorBinding
@Target( {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityCheck {
	String permission() default "READ_STUFF";
}
``` 
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

## Note
In the interceptor code:
```
@Interceptor
@SecurityCheck(permission = "SAY_HI")  
public class SecurityCheckInterceptor {
. . .
```
This will trigger the interceptor for the very specific value `(permission = "SAY_HI")` of the permission.

To have the interceptor triggered for any value (possibly analyzed in the interceptor), have the interceptor written like this:
```java
@Interceptor
@SecurityCheck 
public class SecurityCheckInterceptor {
. . .
```
and the code to intercept like that (class level here):
```java
@Path("/greet")
@RequestScoped
@Interceptors(SecurityCheckInterceptor.class)
public class GreetResource {
  . . .

```

### Repeatable Annotations
In case you want the same annotation repeated several times at the same level, it need to be repeatable.
```java
    @Path("/{name}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @SecurityCheck(permission = "SAY_HI")
    @SecurityCheck(permission = "DIS_BONJOUR")
    public JsonObject getMessage(@PathParam("name") String name) {
      . . .
    
``` 
The repeatable annotation is defined as an array:
```java
@Inherited
@InterceptorBinding
@Repeatable(SecurityCheck.List.class)
@Target( {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityCheck {
	String permission() default "READ_STUFF";

	// Required to repeat the annotation (more tan once at the same place)
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.METHOD})
	@interface List {
		SecurityCheck[] value();
	}
}
```
From the interceptor's code, it is consider as another type of annotation:
```java
@Interceptor
@SecurityCheck
public class SecurityCheckInterceptor {

	@AroundInvoke
	public Object checkSecurity(InvocationContext context) throws Exception {

		List<String> permissionList = new ArrayList<>();
		if (context.getMethod().isAnnotationPresent(SecurityCheck.class)) {
			SecurityCheck securityCheck = context.getMethod().getAnnotation(SecurityCheck.class);
			permissionList.add(securityCheck.permission());
			System.out.println(String.format("Permission: %s", permissionList.stream().collect(Collectors.joining(", "))));
		}

		if (context.getMethod().isAnnotationPresent(SecurityCheck.List.class)) {
			SecurityCheck.List securityChecks = context.getMethod().getAnnotation(SecurityCheck.List.class);
			Arrays.asList(securityChecks.value()).forEach(securityCheck -> permissionList.add(securityCheck.permission()));
			System.out.println(String.format("Permission: %s", permissionList.stream().collect(Collectors.joining(", "))));
		}

  . . .
```
See the code for more details on the interceptor.

---

