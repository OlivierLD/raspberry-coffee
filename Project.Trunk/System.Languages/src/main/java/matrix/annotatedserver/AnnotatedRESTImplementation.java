package matrix.annotatedserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import matrix.PolynomUtil;
import matrix.SquareMatrix;
import matrix.SystemUtil;

import java.io.StringReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * Unlike in the non-annotated case, Operations need to be non-private.
 *
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * </p>
 */
@RootPath(path = "/math")
public class AnnotatedRESTImplementation {

	private boolean verbose = "true".equals(System.getProperty("math.rest.verbose"));

	private MathRequestManager mathRequestManager;

	public AnnotatedRESTImplementation(MathRequestManager restRequestManager) {
		this.mathRequestManager = restRequestManager;
	}

	@OperationDefinition(
			verb = OperationDefinition.Verbs.GET,
			path = "/oplist",
			absolutePath = true,
			description = "List of all available operations, on all request managers."
	)
	protected Response getOperationList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<Operation> opList = this.mathRequestManager.getAllOperationList(); // Aggregates ops from all request managers
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	/**
	 * Verb is POST.
	 * Expects request payload as a SystemUtil.SolveSystemInput, containing:
	 * - SquareMatrix
	 * - Coefficient as double[]
	 * Example:
	 * {
	 *   "sma": {
	 *   "dimension": 3,
	 *   "matrixElements": [
	 *       [
	 *         109.81006821762433,
	 *         55.09654400242172,
	 *         128.66890485184516
	 *       ],
	 *       [
	 *         172.7939805400202,
	 *         95.80419573216672,
	 *         194.02793968037076
	 *       ],
	 *       [
	 *         14.211943768383751,
	 *         193.43298203916092,
	 *         120.58194614411559
	 *       ]
	 *     ]
	 *   },
	 *   "coeff": [
	 *     140.31492892548835,
	 *     67.51384221601239,
	 *     122.84483370046004
	 *   ]
	 * }
	 *
	 * Return a double[] in the response's payload
	 *
	 * @param request
	 * @return
	 */
	@OperationDefinition(
			verb = OperationDefinition.Verbs.POST,
			path = "/system-resolution",
			description = "Solves a system of equations"
	)
	protected Response postSystemResolution(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);
		String payload = new String(request.getContent());
		if (!"null".equals(payload)) {
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(payload);
			String errMess = "";
			SolveSystemInput options;
			try {
				options = gson.fromJson(stringReader, SolveSystemInput.class);

				SquareMatrix sma = options.getSma();
				double[] coeff = options.getCoeff();

				double[] solution = SystemUtil.solveSystem(sma, coeff);

				String content = new Gson().toJson(solution);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
			} catch (Exception ex) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("MATH-0003")
								.errorMessage(errMess));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0002")
							.errorMessage("missing mandatory payload for POST request")
							.errorStack(HTTPServer.dumpException(new Throwable())));
			return response;
		}
		return response;
	}

	private static class SolveSystemInput {
		SquareMatrix sma;
		double[] coeff;

		public SolveSystemInput squareMatrix(SquareMatrix sma) {
			this.sma = sma;
			return this;
		}
		public SolveSystemInput coeff(double[] coeff) {
			this.coeff = coeff;
			return this;
		}

		public SquareMatrix getSma() {
			return this.sma;
		}
		public double[] getCoeff() {
			return this.coeff;
		}
	}

	private static class CalculateCurveRequest {
		double[] coeff;
		double from;
		double to;
		double step;

		public CalculateCurveRequest coeff(double[] coeff) {
			this.coeff = coeff;
			return this;
		}
		public CalculateCurveRequest from(double from) {
			this.from = from;
			return this;
		}
		public CalculateCurveRequest to(double to) {
			this.to = to;
			return this;
		}
		public CalculateCurveRequest step(double step) {
			this.step = step;
			return this;
		}
	}

	private static class SmoothRequest {
		List<PolynomUtil.Point> points;
		int degree;

		public SmoothRequest points(List<PolynomUtil.Point> points) {
			this.points = points;
			return this;
		}
		public SmoothRequest degree(int degree) {
			this.degree = degree;
			return this;
		}
	}

	private static class BestSmoothRequest {
		List<PolynomUtil.Point> points;
		int degreeMin;
		int degreeMax;

		public BestSmoothRequest points(List<PolynomUtil.Point> points) {
			this.points = points;
			return this;
		}
		public BestSmoothRequest degreeMin(int degreeMin) {
			this.degreeMin = degreeMin;
			return this;
		}
		public BestSmoothRequest degreeMax(int degreeMax) {
			this.degreeMax = degreeMax;
			return this;
		}
	}

	/**
	 * Verb is POST
	 *
	 * Payload parameters (in a CalculateCurveRequest):
	 * - array of coeffs
	 * - from X
	 * - to X
	 * - step
	 *
	 * Payload looks like this:
	 *
	 * {
	 *     "coeff": [
	 *         -1.23,
	 *         2.34,
	 *         3.45,
	 *         -4.56
	 *     ],
	 *     "from": 0,
	 *     "to": 500,
	 *     "step": 1
	 * }
	 *
	 * @param request
	 * @return
	 */
	@OperationDefinition(
			verb = OperationDefinition.Verbs.POST,
			path = "/calculate-curve",
			description = "Calculate a curve, requires coeff, from, to, optional step."
	)
	protected Response calculateCurve(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);
		String payload = new String(request.getContent());
		if (!"null".equals(payload)) {
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(payload);
			String errMess = "";
			CalculateCurveRequest options;
			try {
				options = gson.fromJson(stringReader, CalculateCurveRequest.class);

				double[] coeff = options.coeff;
				double from = options.from;
				double to = options.to;
				double step = 1;
				if (options.step != 0) { // TODO Check if positive
					step = options.step;
				}
				List<PolynomUtil.Point> curve = new ArrayList<>();
				for (double x=from; x<=to; x+=step) {
					double y = PolynomUtil.f(coeff, x);
					curve.add(new PolynomUtil.Point(x, y));
				}

				String content = new Gson().toJson(curve);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
			} catch (Exception ex) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("MATH-0003")
								.errorMessage(errMess));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0002")
							.errorMessage("missing mandatory payload for POST request")
							.errorStack(HTTPServer.dumpException(new Throwable())));
			return response;
		}
		return response;
	}

	/**
	 * Verb is POST
	 *
	 * @param request requires a SmoothRequest in the request payload.
	 * @return
	 */
	@OperationDefinition(
			verb = OperationDefinition.Verbs.POST,
			path = "/smooth",
			description = "Smooth a cloud of points. Requires a SmoothRequest in the payload."
	)
	protected Response smooth(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);
		String payload = new String(request.getContent());
		if (!"null".equals(payload)) {
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(payload);
			String errMess = "";
			SmoothRequest options;
			try {
				options = gson.fromJson(stringReader, SmoothRequest.class);
				double[] solution = SystemUtil.smooth(options.points, options.degree);

				String content = new Gson().toJson(solution);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
			} catch (Exception ex) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("MATH-0303")
								.errorMessage(errMess));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0302")
							.errorMessage("missing mandatory payload for POST request")
							.errorStack(HTTPServer.dumpException(new Throwable())));
			return response;
		}
		return response;
	}

	/**
	 * Verb is POST
	 *
	 * @param request requires a BestSmoothRequest in the request payload.
	 * @return
	 */
	@OperationDefinition(
			verb = OperationDefinition.Verbs.POST,
			path = "/intelligent-smooth",
			description = "Smooth a cloud of points. Requires a BestSmoothRequest in the payload."
	)
	protected Response intelligentSmooth(Request request) {
		Response response = new Response(request.getProtocol(), Response.CREATED);
		String payload = new String(request.getContent());
		if (!"null".equals(payload)) {
			Gson gson = new GsonBuilder().create();
			StringReader stringReader = new StringReader(payload);
			String errMess = "";
			BestSmoothRequest options;
			try {
				options = gson.fromJson(stringReader, BestSmoothRequest.class);
				Map<Integer, Double> minimalDistances = new HashMap<>();
				Map<Integer, double[]> coeffs = new HashMap<>();
				long before = 0L;
				for (int degree=options.degreeMin; degree<=options.degreeMax; degree++) {
					if (verbose) {
						System.out.println(String.format("------------- D E G R E E  %d ---------------------", degree));
						System.out.println(String.format("IntelligentSmooth: Smoothing, degree %d, %d points.", degree, options.points.size()));
						before = System.currentTimeMillis();
					}
					double[] solution = SystemUtil.smooth(options.points, degree);
					if (verbose) {
						System.out.println(String.format("IntelligentSmooth: Smooth took %s ms. Calculating minimal distances, degree %d, %d points.",
								NumberFormat.getInstance().format(System.currentTimeMillis() - before),
								degree,
								options.points.size()));
						before = System.currentTimeMillis();
					}
					AtomicReference<Double> acc = new AtomicReference<>(0d);
					options.points.stream().forEach(pt -> {
						acc.getAndAccumulate(SystemUtil.minDistanceToCurve(solution, pt), Double::sum);
					});
					if (verbose) {
						System.out.println(String.format("IntelligentSmooth: MinDist took %s ms. Degree %d, min Acc: %f, AVG: %f",
								NumberFormat.getInstance().format(System.currentTimeMillis() - before),
								degree,
								acc.get(),
								(acc.get() / options.points.size())));
					}
					minimalDistances.put(degree, acc.get());
					coeffs.put(degree, solution);
				}
				if (verbose) {
					System.out.println("----------------------------------------------------");
				}
				// Find smallest dist
				Map.Entry<Integer, Double> min = null;
				for (Map.Entry<Integer, Double> entry : minimalDistances.entrySet()) {
					if (min == null || min.getValue() > entry.getValue()) {
						min = entry;
					}
				}
				double[] best = coeffs.get(min.getKey());
				String content = new Gson().toJson(best);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
			} catch (Exception ex) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("MATH-0403")
								.errorMessage(errMess));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0402")
							.errorMessage("missing mandatory payload for POST request")
							.errorStack(HTTPServer.dumpException(new Throwable())));
			return response;
		}
		return response;
	}

	/**
	 * For dev phase.
	 * Verb is GET
	 *
	 * This is a method to expose during development phase.
	 * This allows the developer to see what a Java Object will look like in json.
	 *
	 * Dimension as query string parameter, 'dim=4'.
	 *
	 * @param request
	 * @return
	 */
	@OperationDefinition(
			verb = OperationDefinition.Verbs.GET,
			path = "/random-matrix",
			description = "Return a randomly generated square matrix. 'dim' as QueryString parameter."
	)
	protected Response getRandomMatrix(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> qs = request.getQueryStringParameters();
		if (qs == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0000")
							.errorMessage("missing mandatory query string prm 'dim'")
							.errorStack(HTTPServer.dumpException(new Throwable())));
			return response;
		}
		String dimStr = (qs == null ? null : qs.get("dim"));
		try {
			int dim = Integer.parseInt(dimStr);
			SquareMatrix squareMatrix = new SquareMatrix(dim, true);
			for (int row=0; row<dim; row++) {
				for (int col=0; col<dim; col++) {
					squareMatrix.setElementAt(row, col, 200d * Math.random());
				}
			}
			String content = new Gson().toJson(squareMatrix);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (NumberFormatException nfe) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0001")
							.errorMessage(nfe.toString())
							.errorStack(HTTPServer.dumpException(nfe)));
			return response;
		}
		return response;
	}

	@OperationDefinition(
			verb = OperationDefinition.Verbs.GET,
			path = "/random-system-input",
			description = "Return a randomly generated system input. 'dim' as QueryString parameter."
	)
	protected Response getRandomSystemInput(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> qs = request.getQueryStringParameters();
		if (qs == null) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0100")
							.errorMessage("missing mandatory query string prm 'dim'")
							.errorStack(HTTPServer.dumpException(new Throwable())));
			return response;
		}
		String dimStr = (qs == null ? null : qs.get("dim"));
		try {
			int dim = Integer.parseInt(dimStr);
			SquareMatrix squareMatrix = new SquareMatrix(dim, true);
			for (int row=0; row<dim; row++) {
				for (int col=0; col<dim; col++) {
					squareMatrix.setElementAt(row, col, 200d * Math.random());
				}
			}
			double[] coeff = new double[dim];
			for (int i=0; i<dim; i++) {
				coeff[i] = 200d * Math.random();
			}
			SolveSystemInput prm = new SolveSystemInput()
					.squareMatrix(squareMatrix)
					.coeff(coeff);

			String content = new Gson().toJson(prm);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (NumberFormatException nfe) {
			response = HTTPServer.buildErrorResponse(
					response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0101")
							.errorMessage(nfe.toString())
							.errorStack(HTTPServer.dumpException(nfe))
			);
			return response;
		}
		return response;
	}

	@OperationDefinition(
			verb = OperationDefinition.Verbs.GET,
			path = "/random-curve-request",
			description = "Return a CalculateCurveRequest."
	)
	protected Response getRandomCalculateCurveRequest(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			CalculateCurveRequest ccr = new CalculateCurveRequest()
					.coeff(new double[] { 1, 2, 3, 4 })
					.from(0)
					.to(500)
					.step(1);
			String content = new Gson().toJson(ccr);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception nfe) {
			response = HTTPServer.buildErrorResponse(
					response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("MATH-0201")
							.errorMessage(nfe.toString())
							.errorStack(HTTPServer.dumpException(nfe))
			);
			return response;
		}
		return response;
	}

	/**
	 * Can be used as a temporary placeholder when creating a new operation.
	 *
	 * @param request
	 * @return
	 */
	private Response emptyOperation(Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}
}
