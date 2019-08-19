## Feasibility test
An annotated REST server, like JAX-RS (Jersey -> Polo).

![Polo](./polo.jpg)

- The main is `restserver.PoloServer`.
- The request manager is `restserver.PoloRESTRequestManager implements http.RESTRequestManager`.
- The implementation - with its annotations is in `restserver.AnnotatedRESTImplementation`.
