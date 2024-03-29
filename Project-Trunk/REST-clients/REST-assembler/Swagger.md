## Swagger generation

#### From the Command Line Interface (CLI)
- Old vs New: <https://openapi-generator.tech/docs/swagger-codegen-migration>
- On a Mac : `brew install openapi-generator`
- Everywhere, with node: `npm install @openapitools/openapi-generator-cli -g`
- Or also: `wget http://central.maven.org/maven2/org/openapitools/openapi-generator-cli/3.3.4/openapi-generator-cli-3.3.4.jar -O openapi-generator-cli.jar`
- `$ openapi-generator config-help --generator-name jaxrs-jersey`

For Java/JAXRS
```
 $ swagger-codegen generate --lang jaxrs-jersey --input-spec sensors.v3.yaml --output ./swag-gen/jaxrs --api-package sensors.io --verbose
```
or more recently
```
 $ openapi-generator generate --generator-name jaxrs-jersey --input-spec sensors.v3.yaml --output ./swag-gen/jaxrs --package-name sensors.io --verbose
```

For NodeJS
```
 $ swagger-codegen generate --lang nodejs-server --input-spec sensors.v3.yaml --output ./swag-gen/node
```
or more recently
```
 $ openapi-generator generate --generator-name nodejs-express-server --input-spec sensors.v3.yaml --output ./swag-gen/node --verbose
```

For Scala
```
 $ swagger-codegen generate --lang scala-akka-http-server --input-spec sensors.v3.yaml --output ./swag-gen/scalaserver
```
or more recently
```
 $ openapi-generator generate --generator-name scalatra --input-spec sensors.v3.yaml --output ./swag-gen/node --verbose
```
...etc.

The list of generators is obtained by typing
```
 $ openapi-generator list
``` 
 
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

- Explicitly add the code in Bootstrap, web.xml.
- Explicitly add the `@Context ServletContext` parameters to interfaces and implementation if you wish to use it.
    - See [here](https://openapi-generator.tech/docs/templating) to learn about customizing mustache templates.
