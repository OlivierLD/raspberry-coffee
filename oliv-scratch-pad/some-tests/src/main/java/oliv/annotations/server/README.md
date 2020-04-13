The idea is to show the foundations of a very basic JAX-RS like process.

There is an `OperationDefinition` annotation, used to annotate
a `SampleServerDefinition`.

At runtime, the `ServerRunner` kicks in, read the annotations, and builds 
a list of operations invoking the annotated methods on request.

 
