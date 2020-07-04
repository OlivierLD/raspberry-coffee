package navrest;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.HttpHeaders;
import http.RESTProcessorUtil;
import utils.SystemUtils;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * <br>
 * Those operation mostly retrieve the state of the SunFlower class, and device.
 * <br>
 * The SunFlower will use the {@link #processRequest(Request)} method of this class to
 * have the required requests processed.
 * </p>
 */
public class RESTImplementation {

	private NavRequestManager navRequestManager;

	private final static String SERVER_PREFIX = "/server";
	private final static String WW_PREFIX = "/ww";
	private final static String NAV_PREFIX = "/nav";
	private final static String FEATHER_PREFIX = "/feather";

	private final static boolean VERBOSE = "true".equals(System.getProperty("rest.verbose"));

	public RESTImplementation(@Nonnull NavRequestManager restRequestManager) {

		this.navRequestManager = restRequestManager;
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);
	}

	/**
	 * Define all the REST operations to be managed
	 * by the HTTP server.
	 * <p>
	 * Frame path parameters with curly braces.
	 * <p>
	 * See {@link #processRequest(Request)}
	 * See {@link HTTPServer}
	 */
	private List<Operation> operations = Arrays.asList(
			new Operation(
					"GET",
					"/oplist", // Yes, no prefix here.
					this::getOperationList,
					"List of all available operations, on all request managers."),
			/*
			 * This resource involves both the Routing (for the GRIB) and the ImageProcessing (for the faxes) services.
			 * This is why it is here. It may go somewhere else in the future...
			 */
			new Operation(
					"GET",
					WW_PREFIX + "/composite-hierarchy", // QS Prm: filter
					this::getCompositeHierarchy,
					"Retrieve the list of the composites already available on the file system"),

			new Operation(
					"GET",
					NAV_PREFIX + "/polar-file-location",
					this::getPolarFileLocation,
					"Returns the polar file location passed as System variable."),
//			new Operation(
//					"GET",
//					NAV_PREFIX + "/dev-curve",
//					this::getDeviationCurve,
//					"Returns the deviation curve as a JSON Object."),

			new Operation(
					"POST",
					FEATHER_PREFIX + "/lifespan",
					this::setFeatherLifespan,
					"A small utility used to evaluate the lifespan of a feather running on a LiPo battery."),
			new Operation(
					"GET",
					FEATHER_PREFIX + "/lifespan",
					this::getFeatherLifespan,
					"Get the last value set by the above."),
			new Operation(
					"GET",
					SERVER_PREFIX + "/networks",
					this::getNetworks,
					"Get the list of the networks the server is on."),
			new Operation(
					"GET",
					SERVER_PREFIX + "/ip-address",
					this::getIpAddress,
					"Get IP Address (Linux only)."),
			new Operation(
					"GET",
					SERVER_PREFIX + "/cpu-load",
					this::getCPULoad,
					"Get CPU Load (Linux only)."),
			new Operation(
					"GET",
					SERVER_PREFIX + "/cpu-temperature",
					this::getCPUTemperature,
					"Get CPU Temperature (Linux only)."),
			new Operation(
					"GET",
					SERVER_PREFIX + "/disk-usage",
					this::getDiskUsage,
					"Get Disk Usage (Linux only)."),
			new Operation(
					"GET",
					SERVER_PREFIX + "/memory-usage",
					this::getMemoryUsage,
					"Get Memory Usage (Linux only)."),
			new Operation(
					"GET",
					SERVER_PREFIX + "/system-data",
					this::getSystemData,
					"Get all system data (Linux only)."),
			new Operation(
					"GET",
					SERVER_PREFIX + "/addresses", // Optional QS Prm: v4Only=true|[false], iface=wlan0
					this::getIps,                       // Returns couples like ("iface", "address")
					"Get the list of IP addresses of the server, with the interface names. QS prms: v4Only [false]|true, iface=XXX (optional)")
	);

	protected List<Operation> getOperations() {
		return this.operations;
	}

	/**
	 * This is the method to invoke to have a REST request processed as defined above.
	 *
	 * @param request as it comes from the client
	 * @return the actual result.
	 */
	public Response processRequest(@Nonnull Request request) throws UnsupportedOperationException {
		Optional<Operation> opOp = operations
				.stream()
				.filter(op -> op.getVerb().equals(request.getVerb()) && RESTProcessorUtil.pathMatches(op.getPath(), request.getPath()))
				.findFirst();
		if (opOp.isPresent()) {
			Operation op = opOp.get();
			request.setRequestPattern(op.getPath()); // To get the prms later on.
			Response processed = op.getFn().apply(request); // Execute here.
			return processed;
		} else {
			throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
		}
	}

	private Response getOperationList(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<Operation> opList = this.navRequestManager.getAllOperationList(); // Aggregates ops from all request managers
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	private Response getCompositeHierarchy(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> qs = request.getQueryStringParameters();
		String filter = (qs == null ? null : qs.get("filter")); // Filter on the COMPOSITE name.
		try {
			// compositeHierarchy is still ordered.
			Map<String, Object> compositeHierarchy = new CompositeCrawler().getCompositeHierarchy(filter);
			String content = new Gson().toJson(compositeHierarchy);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("COMP-0001")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getPolarFileLocation(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String content = System.getProperty("polar.file.location");
			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("NAV-0001")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response setFeatherLifespan(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				System.out.println("Feather Service received:" + payload);
				NavServerContext.getInstance().put("FEATHER_LIFESPAN", payload);
			}
		}
		String content = "OK";
		RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	private Response getFeatherLifespan(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String content = "";
			try {
				content = NavServerContext.getInstance().get("FEATHER_LIFESPAN").toString();
			} catch (NullPointerException npe) {
				// Missing, no worries.
				content = "null";
			}
			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("FEATHER-0001")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}


	private Response getNetworks(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			List<String> networkName = SystemUtils.getNetworkName();
			String content = new Gson().toJson(networkName);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0001")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getIps(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> qs = request.getQueryStringParameters();
		boolean v4Only = qs != null && "true".equals(qs.get("v4Only"));
		String iFace = qs != null ? qs.get("iface") : null;
		try {
			List<String[]> ipAddresses = SystemUtils.getIPAddresses(iFace, v4Only);
			String content = new Gson().toJson(ipAddresses);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0002")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getIpAddress(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String ipAddress = SystemUtils.getIPAddress();
			String content = ipAddress; // new Gson().toJson(ipAddress);

			if (VERBOSE) {
				System.out.println(String.format("%s => %s", ipAddress, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0003")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getCPUTemperature(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String cpuTemperature = SystemUtils.getCPUTemperature();
			String content = cpuTemperature; // new Gson().toJson(cpuTemperature);

			if (VERBOSE) {
				System.out.println(String.format("%s => %s", cpuTemperature, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0004")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getDiskUsage(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String diskUsage = SystemUtils.getDiskUsage();
			String content = diskUsage; // new Gson().toJson(diskUsage);

			if (VERBOSE) {
				System.out.println(String.format("%s => %s", diskUsage, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0005")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getMemoryUsage(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String memoryUsage = SystemUtils.getMemoryUsage();
			String content = memoryUsage; // new Gson().toJson(memoryUsage);

			if (VERBOSE) {
				System.out.println(String.format("%s => %s", memoryUsage, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0006")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	private Response getCPULoad(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String cpuLoad = SystemUtils.getCPULoad();
			String content = cpuLoad; // new Gson().toJson(cpuLoad);

			if (VERBOSE) {
				System.out.println(String.format("%s => %s", cpuLoad, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, HttpHeaders.TEXT_PLAIN, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0007")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
			return response;
		}
		return response;
	}

	// Must be a Java Bean (with getters and setters) for the toJson to work.
	private static class SystemData {
		String ipAddress;
		String cpuTemperature;
		String cpuLoad;
		String memoryUsage;
		String diskUsage;

		public void setIpAddress(String ipAddress) {
			this.ipAddress = ipAddress;
		}

		public void setCpuTemperature(String cpuTemperature) {
			this.cpuTemperature = cpuTemperature;
		}

		public void setCpuLoad(String cpuLoad) {
			this.cpuLoad = cpuLoad;
		}

		public void setMemoryUsage(String memoryUsage) {
			this.memoryUsage = memoryUsage;
		}

		public void setDiskUsage(String diskUsage) {
			this.diskUsage = diskUsage;
		}

		public String getIpAddress() {
			return ipAddress;
		}

		public String getCpuTemperature() {
			return cpuTemperature;
		}

		public String getCpuLoad() {
			return cpuLoad;
		}

		public String getMemoryUsage() {
			return memoryUsage;
		}

		public String getDiskUsage() {
			return diskUsage;
		}
	}
	private Response getSystemData(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			String ipAddress = SystemUtils.getIPAddress();
			String cpuLoad = SystemUtils.getCPULoad();
			String cpuTemperature = SystemUtils.getCPUTemperature();
			String memoryUsage = SystemUtils.getMemoryUsage();
			String diskUsage = SystemUtils.getDiskUsage();
			SystemData systemData = new SystemData();
			systemData.ipAddress = ipAddress;
			systemData.cpuTemperature = cpuTemperature;
			systemData.cpuLoad = cpuLoad;
			systemData.memoryUsage = memoryUsage;
			systemData.diskUsage = diskUsage;

			String content = new Gson().toJson(systemData);

			if (VERBOSE) {
				System.out.println(String.format("%s => %s", systemData, content));
			}

			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SERVER-0008")
							.errorMessage(ex.toString())
							.errorStack(HTTPServer.dumpException(ex)));
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
	private Response emptyOperation(@Nonnull Request request) {
		Response response = new Response(request.getProtocol(), Response.NOT_IMPLEMENTED);
		return response;
	}
}
