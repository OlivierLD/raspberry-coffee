package restserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.RESTProcessorUtil;
import http.RESTRequestManager;
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
import java.util.stream.Collectors;

/**
 * Annotated RESTServer
 *
 * Notice the methods {@link #buildOperationList} and {@link #processRequest}
 */
public class PoloRESTRequestManager implements RESTRequestManager {

	private boolean httpVerbose = "true".equals(System.getProperty("http.verbose"));
	private boolean restVerbose = "true".equals(System.getProperty("server.rest.verbose"));

	private AnnotatedRESTImplementation restImplementation;

	private PoloServer poloServer;
	private List<RESTOperation> operations;

	public enum PrmType {
		QUERY, PATH, BODY
	}
	public static class RestParameter {
		private PrmType type; // Location
		private Class prmClass;
		private String prmName; // For Query and Path params
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
			root = rootPath.value();
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

				Annotation[][] parameterAnnotations = method.getParameterAnnotations();
				if (parameterAnnotations != null && parameterAnnotations.length > 0) {
					List<Integer> missingAnnotations = new ArrayList<>();
					for (int i=0; i<parameterAnnotations.length; i++) {
						if (parameterAnnotations[i].length == 0) {
							missingAnnotations.add(i + 1);
						}
					}
					if (missingAnnotations.size() > 0) {
						// Mismatch
						throw new RuntimeException(String.format("All parameters must be annotated. Class %s, method %s, missing annotation for parameter(s) %s",
								server.getName(),
								method.getName(),
								missingAnnotations.stream().map(String::valueOf).collect(Collectors.joining(", ")) ));
					}
					for (int prmIdx = 0; prmIdx < parameterAnnotations.length; prmIdx++) {
						if (parameterAnnotations[prmIdx] != null && parameterAnnotations[prmIdx].length > 0) {
							RestParameter restParameter = new RestParameter();
							String paramAnnotation = "";
							if (parameterAnnotations[prmIdx][0] instanceof QueryParam) {
								paramAnnotation = QueryParam.class.getName();
								restParameter.type = PrmType.QUERY;
								restParameter.prmName = ((QueryParam)parameterAnnotations[prmIdx][0]).name();
								String typeName = parameterTypes[prmIdx].getName();
								if (!QueryParam.supportedTypes.contains(typeName)) {
									throw new RuntimeException(String.format("Class %s, method %s: Type %s not supported for QueryParam, must be in %s",
											server.getName(),
											method.getName(),
											typeName,
											QueryParam.supportedTypes.stream().collect(Collectors.joining(", "))));
								}
								restParameter.prmScalarType = typeName;
							} else if (parameterAnnotations[prmIdx][0] instanceof BodyParam) {
								paramAnnotation = BodyParam.class.getName();
								restParameter.type = PrmType.BODY;
								restParameter.prmClass = parameterTypes[prmIdx]; // ((BodyParam)parameterAnnotations[prmIdx][0]).type();
							} else if (parameterAnnotations[prmIdx][0] instanceof PathParam) {
								paramAnnotation = PathParam.class.getName();
								restParameter.type = PrmType.PATH;
								restParameter.prmName = ((PathParam)parameterAnnotations[prmIdx][0]).name();
								// Make sure the parameter is defined in the path.
								String methodPath = String.format("%s%s", (operation.absolutePath() ? "" : root), operation.path());
								if (!methodPath.contains("{" + restParameter.prmName + "}")) {
									throw new RuntimeException(String.format("Class %s, method %s, parameter {%s} not found in the operation path %s ",
											server.getName(),
											method.getName(),
											restParameter.prmName,
											methodPath));
								}
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

		HTTPServer.Response response = new HTTPServer.Response(request.getProtocol(), HTTPServer.Response.STATUS_OK);

		if (opOp.isPresent()) {
			RESTOperation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the prms later on.
			// Invoke the embedded method through a function built here, with all method's parameters, and returned value.

			String payload = new String(request.getContent()); // For Body Parameters
			Map<String, String> qs = request.getQueryStringParameters(); // For Query Parameters
			List<String> pathParameterNames = request.getPathParameterNames(); // For Path Parameters
			List<String> pathParameters = request.getPathParameters();

			Object processed = null; // op.getFn().apply(request); // Execute here.
			List<String> errors = new ArrayList<>();
			List<Object> prms = new ArrayList<>();
			op.prmList.stream().forEach(prm -> {
				if (prm.type == PrmType.QUERY) {
					String pName = prm.prmName;
					if (qs != null) {
						String pValue = qs.get(pName);
						try {
							if (QueryParam.STRING.equals(prm.prmScalarType)) {
								prms.add(pValue);
							} else if (QueryParam.INT.equals(prm.prmScalarType) || QueryParam.INTEGER.equals(prm.prmScalarType)) {
								prms.add(Integer.parseInt(pValue));
							} else if (QueryParam.SHORT.equals(prm.prmScalarType) || QueryParam.SHORT_OBJECT.equals(prm.prmScalarType)) {
								prms.add(Short.parseShort(pValue));
							} else if (QueryParam.DOUBLE.equals(prm.prmScalarType) || QueryParam.DOUBLE_OBJECT.equals(prm.prmScalarType)) {
								prms.add(Double.parseDouble(pValue));
							} else if (QueryParam.FLOAT.equals(prm.prmScalarType) || QueryParam.FLOAT_OBJECT.equals(prm.prmScalarType)) {
								prms.add(Float.parseFloat(pValue));
							} else if (QueryParam.BOOLEAN.equals(prm.prmScalarType)) {
								prms.add(Boolean.valueOf(pValue));
							}
						} catch (NumberFormatException nfe) {
							errors.add(nfe.toString());
							prms.add(null);
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
						errors.add(ex.toString());
						prms.add(null);
					}
					prms.add(value);
				} else if (prm.type == PrmType.BODY) {
					// TODO Make sure there is only ONE BodyParam
					if (!"null".equals(payload)) {
						Gson gson = new GsonBuilder().create();
						StringReader stringReader = new StringReader(payload);
						Object value;
						try {
							value = gson.fromJson(stringReader, prm.getPrmClass());
							prms.add(prm.getPrmClass().cast(value));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					} else {
						prms.add(null);
					}
				}
			});
			if (errors.size() > 0) {
				response = HTTPServer.buildErrorResponse(response,
						HTTPServer.Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("REST-0001")
								.errorMessage(errors.stream().collect(Collectors.joining("\n"))));
				return response;
			}
			Object[] methodPrms = prms.toArray(new Object[prms.size()]);
			try {
				processed = op.method.invoke(op.isStatic ? op.serverClass : op.serverImpl, methodPrms);
				if (restVerbose) {
					System.out.println(String.format("Result is a %s (%s)", op.returnedType.cast(processed).getClass().getName(), op.returnedType.cast(processed).getClass().getTypeName()));
				}
			} catch (IllegalAccessException e) {
				response = HTTPServer.buildErrorResponse(response,
						HTTPServer.Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("REST-0002")
								.errorMessage(e.toString()));
				return response;
			} catch (InvocationTargetException e) {
				response = HTTPServer.buildErrorResponse(response,
						HTTPServer.Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("REST-0003")
								.errorMessage(e.toString()));
				return response;
			}
			String content = new Gson().toJson(op.returnedType.cast(processed)); // Problem with Generic types... like List<CustomStuff>
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;

		} else {
//			response = HTTPServer.buildErrorResponse(response,
//					HTTPServer.Response.NOT_IMPLEMENTED,
//					new HTTPServer.ErrorPayload()
//							.errorCode("REST-0004")
//							.errorMessage(String.format("%s not managed", request.toString())));
//			response.setPayload(String.format("Not implemented on this server %s", this.restImplementation.getClass().getName()).getBytes());
//			return response;
			throw new UnsupportedOperationException(String.format("%s not managed on this server %s", request.toString(), this.restImplementation.getClass().getName()));
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
