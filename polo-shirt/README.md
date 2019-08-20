## Polo Shirt 👕 - Feasibility test
An annotation-based REST server, like JAX-RS (Jersey -> Polo), latching on the [http-tiny-server](../http-tiny-server).

- The main is `restserver.PoloServer`.
- The request manager is `restserver.PoloRESTRequestManager implements http.RESTRequestManager`.
    - Annotations re managed in the method `buildOperationList`.
- The implementation - with its annotations - is in `restserver.AnnotatedRESTImplementation`.
- Annotation definitions are in the package `restserver.annotations`.

#### TODO
Custom Swagger generator?

#### Swagger
> Note the `yaml` structure has changed between version 2 and version 3.

- [Swagger](https://swagger.io/)
- [Swagger Codegen](https://swagger.io/tools/swagger-codegen/)
- [Customizing the generator](https://github.com/swagger-api/swagger-codegen#customizing-the-generator)
- Online yaml editor: <https://app.swaggerhub.com/apis/OlivierLD/PoloShirt.101/1.0.0>

---

- <https://symfony.com/doc/current/components/yaml/yaml_format.html>
- <https://swagger.io/docs/specification/data-models/data-types/>
- <https://gettaurus.org/docs/YAMLTutorial/>

---

> Note: For Swagger and Jetty related stuff, use Java 8, _not_ 9.

#### To install Swagger Codegen
Look [here](https://swagger.io/docs/open-source-tools/swagger-codegen/).

For Linux, one of:
```
 $ wget https://oss.sonatype.org/content/repositories/releases/io/swagger/swagger-codegen-cli/2.2.1/swagger-codegen-cli-2.2.1.jar
 $ wget -e use_proxy=yes -e http_proxy=http://www-proxy.us.oracle.com:80 -e https_proxy=http://www-proxy.us.oracle.com:80 https://oss.sonatype.org/content/repositories/releases/io/swagger/swagger-codegen-cli/2.2.1/swagger-codegen-cli-2.2.1.jar
 $ wget https://repo1.maven.org/maven2/io/swagger/swagger-codegen-cli/3.0.0-rc1/swagger-codegen-cli-3.0.0-rc1.jar
 $ wget -e use_proxy=yes -e http_proxy=http://www-proxy.us.oracle.com:80 -e https_proxy=http://www-proxy.us.oracle.com:80 https://repo1.maven.org/maven2/io/swagger/swagger-codegen-cli/3.0.0-rc1/swagger-codegen-cli-3.0.0-rc1.jar
```
Then to run it (depending on the version you want):
```
 $ java -jar swagger-codegen-cli-2.2.1.jar help
 $ java -jar swagger-codegen-cli-3.0.0-rc1.jar
```

Try that:
```
 $ ./swagger.sh
 $ cd generated/jaxrs
 $ mvn clean package jetty:run [--settings ../../settings.xml] -Dmaven.test.skip=true
```
Then from another console:
```
 $ curl -X GET http://localhost:2345/oplist
 {"code":4,"type":"ok","message":"magic!"}
```
> Note: in IntelliJ, right-click on the pom.xml in generated/jaxrs, and `Add as Maven Project`. 

Flesh out your methods (that one in `samples.io.impl.TopRootApiServiceImpl`):
```java
@Override
public Response greetV3(GreetingRequest body, SecurityContext securityContext) throws NotFoundException {
    // do some magic!
    GreetingMessage response = new GreetingMessage();
    response.setMessage(String.format("%s %s!",
        body != null && body.getSalutation() != null ? body.getSalutation() : "Hello",
        body != null && body.getName() != null ? body.getName() : "World"));
//  return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, response.toString())).build();
    return Response.ok().entity(response).build();
}
```
Rebuild, re-run, and then (notice the simple-quotes around the payload)
```
curl -X POST "http://localhost:2345/top-root/greeting/v3" -H "accept: application/json" -H "Content-Type: application/json" -d '{"name":"Oliv","salutation":"Salut"}'
{"message":"Salut Oliv!"}
```
