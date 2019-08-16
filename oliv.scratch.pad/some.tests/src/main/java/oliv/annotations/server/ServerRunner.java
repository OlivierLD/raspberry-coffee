package oliv.annotations.server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ServerRunner {

	public static class Request {
	}
	public static class Response {
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
		// Class annotation
		if (server.isAnnotationPresent(OperationDefinition.class)) {
			//
		}

		List<Operation> operations = new ArrayList<>();

		for (Method method : server.getDeclaredMethods()) {
			if (method.isAnnotationPresent(OperationDefinition.class)) {
				OperationDefinition operation = method.getAnnotation(OperationDefinition.class);
				System.out.println(String.format("Method %s, used for: %s %s, %s", method.getName(), operation.verb(), operation.path(), operation.description()));
				operations.add(new Operation(
						operation.verb().toString(),
						operation.path(),
						request -> {  // Maybe there is a better way...
							try {
								System.out.println(String.format("Invoking %s, with a %s", method.getName(), request.getClass().getName()));
								Class<?> returnType = method.getReturnType();
								System.out.println(String.format("%s, returns a %s", method.getName(), returnType.getName()));
								Class<?>[] parameterTypes = method.getParameterTypes();
								System.out.println(String.format("Invoking %s, expects %d prms", method.getName(), parameterTypes.length));
								for (Class<?> cls : parameterTypes) {
									System.out.println(String.format("- type %s", cls.getName()));
								}
								return (Response)method.invoke(server, request);
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
