package restserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.RESTProcessorUtil;
import restserver.annotations.BodyParam;
import restserver.annotations.OperationDefinition;
import restserver.annotations.PathParam;
import restserver.annotations.QueryParam;
import restserver.annotations.RootPath;

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Annotated RESTServer
 *
 * Notice the methods {@link #buildOperationList} and {@link #processRequest}
 */
public class PoloRESTRequestManager implements http.RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose"));
	private boolean restVerbose = "true".equals(System.getProperty("server.rest.verbose"));

	private AnnotatedRESTImplementation restImplementation;

	private PoloServer poloServer;
	private List<RESTOperation> operations;

	public enum PrmType {
		QUERY, PATH, BODY
	}
	public static class RestParameter {
		private PrmType type;
		private Class prmClass;
		private String prmName;
		private String prmScalarType; // Used for Query Params

		public RestParameter() {
		}

		public RestParameter type(PrmType type) {
			this.type = type;
			return this;
		}
		public RestParameter prmClass(Class cls) {
			this.prmClass = cls;
			return this;
		}
		public RestParameter prmName(String name) {
			this.prmName = name;
			return this;
		}
		public RestParameter prmScalarType(String type) {
			this.prmScalarType = type;
			return this;
		}

		public void setType(PrmType type) {
			this.type = type;
		}

		public void setPrmClass(Class prmClass) {
			this.prmClass = prmClass;
		}

		public void setPrmName(String prmName) {
			this.prmName = prmName;
		}

		public void setPrmScalarType(String prmScalarType) {
			this.prmScalarType = prmScalarType;
		}

		public PrmType getType() {
			return type;
		}

		public Class getPrmClass() {
			return prmClass;
		}

		public String getPrmName() {
			return prmName;
		}

		public String getPrmScalarType() {
			return prmScalarType;
		}

	}

	public static class RESTOperation {
		String verb;
		String path;
		String description;
		Method method;
		boolean isStatic = false;
		Object serverImpl;
		Class serverClass;
		Class returnedType;
		List<RestParameter> prmList;

		public RESTOperation() {
		}
		/**
		 *
		 * @param verb GET, PUT, POST, or DELETE
		 * @param path can include {parameters}
		 * @param description
		 */
		public RESTOperation(String verb, String path, String description) {
			this.verb = verb;
			this.path = path;
			this.description = description;
		}

		public void setVerb(String verb) {
			this.verb = verb;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setMethod(Method method) {
			this.method = method;
		}

		public void setStatic(boolean aStatic) {
			isStatic = aStatic;
		}

		public void setServerImpl(Object serverImpl) {
			this.serverImpl = serverImpl;
		}

		public void setServerClass(Class serverClass) {
			this.serverClass = serverClass;
		}

		public void setReturnedType(Class returnedType) {
			this.returnedType = returnedType;
		}

		public void setPrmList(List<RestParameter> prmList) {
			this.prmList = prmList;
		}

		public Method getMethod() {
			return method;
		}

		public boolean isStatic() {
			return isStatic;
		}

		public Object getServerImpl() {
			return serverImpl;
		}

		public Class getServerClass() {
			return serverClass;
		}

		public Class getReturnedType() {
			return returnedType;
		}

		public List<RestParameter> getPrmList() {
			return prmList;
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

	}

	/**
	 *
	 * @param parent to be able to refer to all the request managers
	 */
	public PoloRESTRequestManager(PoloServer parent) {
		this.poloServer = parent;
		restImplementation = new AnnotatedRESTImplementation(this);
		operations = buildOperationList(restImplementation);
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(restImplementation.getOperationList());
		if (restVerbose) {
			System.out.println(">> No duplicate operation found, good to go.");
		}
	}

	private List<RESTOperation> buildOperationList(AnnotatedRESTImplementation instance) {
		Class<AnnotatedRESTImplementation> server = (Class<AnnotatedRESTImplementation>)instance.getClass();
		String root = "/";
		// Class annotation, for the root path.
		if (server.isAnnotationPresent(RootPath.class)) {
			RootPath rootPath = server.getAnnotation(RootPath.class);
			root = rootPath.path();
		}

		List<RESTOperation> operations = new ArrayList<>();

		for (Method method : server.getDeclaredMethods()) {
			if (method.isAnnotationPresent(OperationDefinition.class)) {
				OperationDefinition operation = method.getAnnotation(OperationDefinition.class);
				if (restVerbose) {
					System.out.println(String.format("Method %s, used for: %s %s, %s",
							method.getName(),
							operation.verb(),
							String.format("%s%s", (operation.absolutePath() ? "" : root), operation.path()),
							operation.description()));
				}
				RESTOperation restOperation = new RESTOperation(operation.verb().toString(), String.format("%s%s", (operation.absolutePath() ? "" : root), operation.path()), operation.description());

				Class<?> returnType = method.getReturnType();
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (restVerbose) {
					for (Class<?> cls : parameterTypes) {
						System.out.println(String.format("- type %s", cls.getName()));
					}
				}
				restOperation.returnedType = returnType;
				restOperation.method = method;
				// Static?
				restOperation.isStatic = Modifier.isStatic(method.getModifiers());

				restOperation.serverClass = server;
				restOperation.serverImpl = instance;

				restOperation.prmList = new ArrayList<>();

				// TODO Make sure ALL parameters are annotated
				Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				if (parameterAnnotations != null && parameterAnnotations.length > 0) {
					for (int prmIdx = 0; prmIdx < parameterAnnotations.length; prmIdx++) {
						if (parameterAnnotations[prmIdx] != null && parameterAnnotations[prmIdx].length > 0) {
							RestParameter restParameter = new RestParameter();
							String paramAnnotation = "";
							if (parameterAnnotations[prmIdx][0] instanceof QueryParam) {
								paramAnnotation = QueryParam.class.getName();
								restParameter.type = PrmType.QUERY;
								restParameter.prmName = ((QueryParam)parameterAnnotations[prmIdx][0]).name();
								restParameter.prmScalarType = ((QueryParam)parameterAnnotations[prmIdx][0]).type().toString();
							} else if (parameterAnnotations[prmIdx][0] instanceof BodyParam) {
								paramAnnotation = BodyParam.class.getName();
								restParameter.type = PrmType.BODY;
								restParameter.prmClass = ((BodyParam)parameterAnnotations[prmIdx][0]).type();
							} else if (parameterAnnotations[prmIdx][0] instanceof PathParam) {
								paramAnnotation = PathParam.class.getName(); // TODO Verify presence in the path
								restParameter.type = PrmType.PATH;
								restParameter.prmName = ((PathParam)parameterAnnotations[prmIdx][0]).name();
							}
							if (restVerbose) {
								System.out.println(String.format(">> %s Annotated parameter, with %s",
										method.getParameterTypes()[prmIdx].getName(),
										paramAnnotation)); // First annotation only
							}
							if (restParameter != null) {
								restOperation.prmList.add(restParameter);
							}
						}
					}
				}
				operations.add(restOperation);
			} else {
				if (restVerbose) {
					System.out.println(String.format("- Method %s is not annotated.", method.getName()));
				}
			}
		}
		if (restVerbose) {
			System.out.println(String.format(">> Operation list created, server ready to take requests, %d operation(s).", operations.size()));
			operations.stream().forEach(op -> {
				System.out.println(String.format("%s %s", op.verb, op.path));
			});
		}
		return operations;
	}

	public HTTPServer.Response processRequest(HTTPServer.Request request) throws UnsupportedOperationException {
		Optional<RESTOperation> opOp = operations
				.stream()
				.filter(op -> op.getVerb().equals(request.getVerb()) &&
						RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
				.findFirst();
		if (opOp.isPresent()) {
			RESTOperation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the prms later on.
			// Invoke the embedded method through a function built here, with all method's parameters, and returned value.

			HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);
			String payload = new String(request.getContent()); // For Body Parameters
			if (!"null".equals(payload)) {
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				String errMess = "";
			}
			Map<String, String> qs = request.getQueryStringParameters(); // For Query Parameters
			List<String> pathParameterNames = request.getPathParameterNames(); // For Path Parameters
			List<String> pathParameters = request.getPathParameters();

			Object processed = null; // op.getFn().apply(request); // Execute here.

			List<Object> prms = new ArrayList<>();
			op.prmList.stream().forEach(prm -> {
				 if (prm.type == PrmType.QUERY) {
				 	 String pName = prm.prmName;
				 	 if (qs != null) {
					   String pValue = qs.get(pName);
					   if (prm.prmScalarType == QueryParam.PrmTypes.STRING.toString()) {
						   prms.add(pValue);
					   } else if (prm.prmScalarType == QueryParam.PrmTypes.INT.toString()) {
						   prms.add(Integer.parseInt(pValue));
					   } else if (prm.prmScalarType == QueryParam.PrmTypes.SHORT.toString()) {
						   prms.add(Short.parseShort(pValue));
					   } else if (prm.prmScalarType == QueryParam.PrmTypes.DOUBLE.toString()) {
						   prms.add(Double.parseDouble(pValue));
					   } else if (prm.prmScalarType == QueryParam.PrmTypes.FLOAT.toString()) {
						   prms.add(Float.parseFloat(pValue));
					   } else if (prm.prmScalarType == QueryParam.PrmTypes.BOOLEAN.toString()) {
						   prms.add(Boolean.valueOf(pValue));
					   }
				   } else {
				 	 	 prms.add(null);
				   }
				 } else if (prm.type == PrmType.PATH) {
						int nameIndex = pathParameterNames.indexOf("{" + prm.prmName + "}");
						String value = null;
						try {
							value = pathParameters.get(nameIndex);
						} catch (Exception ex) {
							System.err.println("Oops");
						}
						prms.add(value);
				 } else if (prm.type == PrmType.BODY) {
				 	  // TODO
				 }
			});
			Object[] methodPrms = prms.toArray(new Object[prms.size()]);
			try {
				processed = op.method.invoke(op.isStatic ? op.serverClass : op.serverImpl, methodPrms);
				if (restVerbose) {
					System.out.println(String.format("Result is a %s (%s)", op.returnedType.cast(processed).getClass().getName(), op.returnedType.cast(processed).getClass().getTypeName()));
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			String content = new Gson().toJson(op.returnedType.cast(processed)); // Problem with Generic types... like List<CustomStuff>
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;

		} else {
			throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
		}
	}

	@Override
	public boolean containsOp(String verb, String path) {
		return this.getRestOperationList()
				.stream()
				.filter(operation -> operation.getVerb().equals(verb) && RESTProcessorUtil.pathMatches(operation.getPath(), path))
				.findFirst()
				.isPresent();
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
		return null;
//		return operations;
	}

	public List<RESTOperation> getRestOperationList() {
		return operations;
	}

	/*
	 Specific operations
	 */

	protected List<HTTPServer.Operation> getAllOperationList() {
		return poloServer.getAllOperationList();
	}

}
