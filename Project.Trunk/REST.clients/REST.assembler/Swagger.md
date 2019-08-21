## Swagger generation

#### From the Command Line Interface (CLI)
For Java/JAXRS
```
 $ swagger-codegen generate --lang jaxrs-jersey --input-spec sensors.v3.yaml --output ./swaggen/jaxrs --api-package sensors.io --verbose
```
For NodeJS
```
 $ swagger-codegen generate --lang nodejs-server --input-spec sensors.v3.yaml --output ./generated/node
```
For Scala
```
 $ swagger-codegen generate --lang scala-akka-http-server --input-spec sensors.v3.yaml --output ./generated/scalaserver
```
...etc.
 
#### Aug-2019
Moving to Swagger 3.0 (aka OpenAPI 3.0)

To add to the pom for the Java/Jersey generation:
```xml
    <dependency>
      <groupId>oliv.raspi.coffee</groupId>
      <artifactId>ADC</artifactId>
      <version>1.0</version>
    </dependency>
```

