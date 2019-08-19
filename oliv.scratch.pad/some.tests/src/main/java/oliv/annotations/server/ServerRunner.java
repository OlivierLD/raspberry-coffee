package oliv.annotations.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ServerRunner {

	public static class Request {
	}
	public static class Response {
		Object payload;
	}

	public static class Operation {
		String verb;
		String path;
		String description;
		Function<Request, Response> fn;

		/**
		 *
		 * @param verb GET, PUT, POST, or DELETE
		 * @param path can include {parameters}
		 * @param fn
		 * @param description
		 */
		public Operation(String verb, String path, Function<Request, Response> fn, String description) {
			this.verb = verb;
			this.path = path;
			this.description = description;
			this.fn = fn;
		}

		public String getVerb() {
			return verb;
		}

		public String getPath() {
			return path;
		}

		public String getDescription() {
			return description;
		}

		public Function<Request, Response> getFn() {
			return fn;
		}
	}

	public static void main(String... args) {
		Class<SampleServerDefinition> server = SampleServerDefinition.class;
		String root = "/";
		// Class annotation
		if (server.isAnnotationPresent(RootPath.class)) {
			RootPath rootPath = server.getAnnotation(RootPath.class);
			root = rootPath.path();
		}

		List<Operation> operations = new ArrayList<>();

		for (Method method : server.getDeclaredMethods()) {
			if (method.isAnnotationPresent(OperationDefinition.class)) {
				OperationDefinition operation = method.getAnnotation(OperationDefinition.class);
				System.out.println(String.format("Method %s, used for: %s %s, %s", method.getName(), operation.verb(), root + operation.path(), operation.description()));
				Class<?> returnType = method.getReturnType();
				Class<?>[] parameterTypes = method.getParameterTypes();
				for (Class<?> cls : parameterTypes) {
					System.out.println(String.format("- type %s", cls.getName()));
				}
				Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				if (parameterAnnotations != null && parameterAnnotations.length > 0) {
					for (int prmIdx = 0; prmIdx < parameterAnnotations.length; prmIdx++) {
						if (parameterAnnotations[prmIdx] != null && parameterAnnotations[prmIdx].length > 0) {
							String paramAnnotation = "";
							if (parameterAnnotations[prmIdx][0] instanceof QueryParam) {
								paramAnnotation = QueryParam.class.getName();
							} else if (parameterAnnotations[prmIdx][0] instanceof BodyParam) {
								paramAnnotation = BodyParam.class.getName();
							} else if (parameterAnnotations[prmIdx][0] instanceof PathParam) {
								paramAnnotation = PathParam.class.getName();
							}
							System.out.println(String.format(">> %s Annotated parameter, with %s",
									method.getParameterTypes()[prmIdx].getName(),
									paramAnnotation)); // First annotation only
						}
					}
				}


				operations.add(new Operation(
						operation.verb().toString(),
						root + operation.path(),
						request -> {  // Maybe there is a better way...
							try {
								System.out.println(String.format("Invoking %s, with a %s", method.getName(), request.getClass().getName()));
								System.out.println(String.format("%s, returns a %s", method.getName(), returnType.getName()));
								System.out.println(String.format("Invoking %s, expects %d prms", method.getName(), parameterTypes.length));
								Object[] prms = { request };
								if (parameterTypes.length == 2) { // Then we assume it is the sayHello method (this is just for this example).
									prms = new String[] { "Machin", "Salut" };
								}
								Object response = method.invoke(server, prms); // Second prm can be an array
								Response toReturn = null;
								if (response instanceof SampleServerDefinition.Greeting) {
									System.out.println(String.format(">>> %s", response));
									toReturn = new Response();
									toReturn.payload = response;
								} else {
									toReturn = (Response)response;
								}
								return toReturn;
							} catch (Exception ex) {
								throw new RuntimeException(ex);
							}
						},
						operation.description()));
			} else {
				System.out.println(String.format("Method %s is not annotated.", method.getName()));
			}
		}
		System.out.println("Operation list created, server ready to take requests.");
		// Test first operation
		Request testRequest = new Request();
		Response response = operations.get(0).getFn().apply(testRequest);
		System.out.println(String.format("Done!, got a Response: %s", response));
	}
}
