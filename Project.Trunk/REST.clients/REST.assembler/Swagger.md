## Swagger generation

#### From the Command Line Interface (CLI)
Old vs New: <https://openapi-generator.tech/docs/swagger-codegen-migration>


For Java/JAXRS
```
 $ swagger-codegen generate --lang jaxrs-jersey --input-spec sensors.v3.yaml --output ./swag-gen/jaxrs --api-package sensors.io --verbose
```
For NodeJS
```
 $ swagger-codegen generate --lang nodejs-server --input-spec sensors.v3.yaml --output ./swag-gen/node
```
For Scala
```
 $ swagger-codegen generate --lang scala-akka-http-server --input-spec sensors.v3.yaml --output ./swag-gen/scalaserver
```
...etc.
 
#### Aug-2019
Moving to Swagger 3.0 (aka OpenAPI Spec - OAS - 3.0)

To add to the pom for the Java/Jersey generation:
```xml
    <dependency>
      <groupId>com.pi4j</groupId>
      <artifactId>pi4j-core</artifactId>
      <version>1.2-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>oliv.raspi.coffee</groupId>
      <artifactId>ADC</artifactId>
      <version>1.0</version>
    </dependency>
```

Explicitly add the code in Bootstrap, web.xml.
Explicitly add the @Context ServletContext parameters to interfaces and implementation if you wish to use it.
