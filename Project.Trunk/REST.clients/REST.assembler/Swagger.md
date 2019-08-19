## Swagger generation

#### From the Command Line Interface (CLI)
For Java/JAXRS
```
 $ swagger-codegen generate --lang jaxrs-jersey --input-spec sensors.yaml --output ./generated/jaxrs --api-package sensors.io --verbose
```
For NodeJS
```
 $ swagger-codegen generate --lang nodejs-server --input-spec sensors.yaml --output ./generated/node
```
For Scala
```
 $ swagger-codegen generate --lang scala-akka-http-server --input-spec sensors.yaml --output ./generated/scalaserver
```
...etc.
 

