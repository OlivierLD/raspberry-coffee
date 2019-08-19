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
- [Swagger](https://swagger.io/)
- [Swagger Codegen](https://swagger.io/tools/swagger-codegen/)
- [Customizing the generator](https://github.com/swagger-api/swagger-codegen#customizing-the-generator)

