package matrix.annotatedserver;

import http.HTTPServer;
import http.RESTProcessorUtil;
import http.RESTRequestManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Annotated flavor of the MathServer
 *
 * Notice the methods {@link #buildOperationList} and {@link #processRequest}
 */
public class MathRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose"));
	private boolean mathVerbose = "true".equals(System.getProperty("math.rest.verbose"));

	private AnnotatedRESTImplementation restImplementation;

	private MathServer mathServer;
	private List<HTTPServer.Operation> operations;

	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 */
	public MathRequestManager(MathServer parent) {
		this.mathServer = parent;
		restImplementation = new AnnotatedRESTImplementation(this);
		operations = buildOperationList(restImplementation);
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
		if (mathVerbose) {
			System.out.println(">> No duplicate operation found, good to go.");
		}
	}

	@SuppressWarnings("unchecked")
	private List<HTTPServer.Operation> buildOperationList(AnnotatedRESTImplementation instance) {
		Class<AnnotatedRESTImplementation> server = (Class<AnnotatedRESTImplementation>)instance.getClass();
		String root = "/";
		// Class annotation, for the path root.
		if (server.isAnnotationPresent(RootPath.class)) {
			RootPath rootPath = server.getAnnotation(RootPath.class);
			root = rootPath.path();
		}

		List<HTTPServer.Operation> operations = new ArrayList<>();

		for (Method method : server.getDeclaredMethods()) {
			if (method.isAnnotationPresent(OperationDefinition.class)) {
				OperationDefinition operation = method.getAnnotation(OperationDefinition.class);
				if (mathVerbose) {
					System.out.println(String.format("Method %s, used for: %s %s, %s",
							method.getName(), operation.verb(),
							String.format("%s%s", (operation.absolutePath() ? "" : root), operation.path()),
							operation.description()));
				}
				operations.add(new HTTPServer.Operation(
						operation.verb().toString(),
						String.format("%s%s", (operation.absolutePath() ? "" : root), operation.path()),
						request -> {  // Maybe there is a better way?..
							try {
								Class<?> returnType = method.getReturnType();
								Class<?>[] parameterTypes = method.getParameterTypes();
								if (mathVerbose) {
									System.out.println(String.format("Invoking %s, with a %s", method.getName(), request.getClass().getName()));
									System.out.println(String.format("%s, returns a %s", method.getName(), returnType.getName()));
									System.out.println(String.format("Invoking %s, expects %d prm(s)", method.getName(), parameterTypes.length));
									for (Class<?> cls : parameterTypes) {
										System.out.println(String.format("- type %s", cls.getName()));
									}
								}
								return (HTTPServer.Response)method.invoke(instance, request);
							} catch (Exception ex) {
								throw new RuntimeException(ex);
							}
						},
						operation.description()));
			} else {
				if (mathVerbose) {
					System.out.println(String.format("- Method %s is not annotated.", method.getName()));
				}
			}
		}
		if (mathVerbose) {
			System.out.println(">> Operation list created, server ready to take requests.");
		}
		return operations;
	}

	public HTTPServer.Response processRequest(HTTPServer.Request request) throws UnsupportedOperationException {
		Optional<HTTPServer.Operation> opOp = operations
				.stream()
				.filter(op -> op.getVerb().equals(request.getVerb()) &&
						RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
				.findFirst();
		if (opOp.isPresent()) {
			HTTPServer.Operation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the prms later on.
			// TODO See how to invoke the embedded method through a function built here, with all method's parameters, and returned value.
			HTTPServer.Response processed = op.getFn().apply(request); // Execute here.
			return processed;
		} else {
			throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
		}
	}

	/**
	 * Manage the REST requests.
	 *
	 * @param request incoming request
	 * @return as defined in the {@link AnnotatedRESTImplementation}
	 * @throws UnsupportedOperationException
	 */
	@Override
	public HTTPServer.Response onRequest(HTTPServer.Request request) throws UnsupportedOperationException {
		HTTPServer.Response response = this.processRequest(request); // All the skill is here.
		if (this.httpVerbose) {
			System.out.println("======================================");
			System.out.println("Request :\n" + request.toString());
			System.out.println("Response :\n" + response.toString());
			System.out.println("======================================");
		}
		return response;
	}

	@Override
	public List<HTTPServer.Operation> getRESTOperationList() {
		return operations;
	}

	/*
	 Specific operations
	 */

	protected List<HTTPServer.Operation> getAllOperationList() {
		return mathServer.getAllOperationList();
	}

}
