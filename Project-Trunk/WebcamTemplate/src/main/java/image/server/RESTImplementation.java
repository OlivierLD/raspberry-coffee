package image.server;

import com.google.gson.Gson;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import image.snap.SnapSnapSnap;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * </p>
 */
public class RESTImplementation {

	private final boolean verbose = "true".equals(System.getProperty("image.rest.verbose"));

	private final SnapRequestManager snapRequestManager;
	private final static String SNAP_RESOURCE_PREFIX = "/snap";

	public RESTImplementation(SnapRequestManager restRequestManager) {

		this.snapRequestManager = restRequestManager;
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
	private final List<Operation> operations = Arrays.asList(
			new Operation(
					"GET",
					"/oplist",
					this::getOperationList,
					"List of all available operations, on all request managers."),
			/*
			 * Specific to this RequestManager
			 */
			new Operation(
					"GET",
					SNAP_RESOURCE_PREFIX + "/last-snapshot",
					this::getLastSnapshot,
					"Return the last snapshot name."),
			new Operation(
					"GET",
					SNAP_RESOURCE_PREFIX + "/snap-status",
					this::getSnapThreadStatus,
					"Return the snapshot thread status."),
			new Operation(
					"POST",
					SNAP_RESOURCE_PREFIX + "/commands/{cmd}",
					this::snapThreadCommand,
					"Stop, start, or configure the snap thread. See headers for 'start' and 'config'.")
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
	public Response processRequest(Request request) throws UnsupportedOperationException {
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

	private Response getOperationList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<Operation> opList = this.snapRequestManager.getAllOperationList(); // Aggregates ops from all request managers
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}

	public static class SnapPayload {
		String status;
		String snapUrl;
		String fullPath;

		public SnapPayload status(String status) {
			this.status = status;
			return this;
		}
		public SnapPayload snapUrl(String snapUrl) {
			this.snapUrl = snapUrl;
			return this;
		}
		public SnapPayload fullPath(String fullPath) {
			this.fullPath = fullPath;
			return this;
		}
	}

	/**
	 * Verb is GET
	 *
	 * @param request
	 * @return
	 */
	private Response getSnapThreadStatus(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		try {
			SnapSnapSnap.SnapStatus snapThreadStatus = this.snapRequestManager.getSnapshotServer().getSnapThreadStatus();
			String content = new Gson().toJson(snapThreadStatus);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			String content = new Gson().toJson(ex);
			response.setStatus(Response.BAD_REQUEST);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		}
	}

	public final static String STOP_QS_PRM = "stop";
	public final static String START_QS_PRM = "start";
	public final static String CONFIG_QS_PRM = "config";

	public final static String CAMERA_ROT_HEADER_NAME = "camera-rot";
	public final static String CAMERA_WIDTH_HEADER_NAME = "camera-width";
	public final static String CAMERA_HEIGHT_HEADER_NAME = "camera-height";
	public final static String CAMERA_WAIT_HEADER_NAME = "camera-wait";
	public final static String CAMERA_SNAP_NAME_HEADER_NAME = "camera-snap-name";
	public final static String CAMERA_SNAP_TIME_BASED_NAME_HEADER_NAME = "camera-snap-time-based-name";

	/**
	 * Start or Stop the thread
	 *
	 * Parameters for stop motion (filename pattern) comes from a system variable, -Dtime.based.snap.name=true|false
	 *
	 * Verb is POST
	 *
	 * @param request
	 * @return
	 */
	private Response snapThreadCommand(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<String> pathPrms = request.getPathParameters();
		if (pathPrms.size() != 1) { // Barf
			String errMess = "SNAP-0001: Expected 1 path parameter.";
			String content = new Gson().toJson(errMess);
			response.setStatus(Response.BAD_REQUEST);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		}
		boolean stop = STOP_QS_PRM.equals(pathPrms.get(0));
		boolean start = START_QS_PRM.equals(pathPrms.get(0));
		boolean config = CONFIG_QS_PRM.equals(pathPrms.get(0));

		if (!start && !stop && !config) {
			String errMess = String.format("SNAP-0002: path parameter must be 'config', 'start' or 'stop', not [%s]", pathPrms.get(0));
			String content = new Gson().toJson(errMess);
			response.setStatus(Response.BAD_REQUEST);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		}

		if (stop) {
			if (verbose) {
				System.out.println("Stopping the Snap Thread");
			}
			try {
				this.snapRequestManager.getSnapshotServer().stopSnapThread();
			} catch (Exception ex) {
				String content = new Gson().toJson(ex);
				response.setStatus(Response.BAD_REQUEST);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
				return response;
			}
		} else if (start) {
			if (verbose) {
				System.out.println("Starting the Snap Thread");
			}
			try {
				SnapSnapSnap.SnapStatus snapThreadStatus;
				try {
					snapThreadStatus = this.snapRequestManager.getSnapshotServer().getSnapThreadStatus();
				} catch (Exception ex) {
					String content = new Gson().toJson(ex);
					response.setStatus(Response.BAD_REQUEST);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
					return response;
				}
				Map<String, String> requestHeaders = request.getHeaders();
				if (snapThreadStatus != null) {
					if (requestHeaders != null) {
						String cameraRotStr = requestHeaders.get(CAMERA_ROT_HEADER_NAME);
						String cameraWidthStr = requestHeaders.get(CAMERA_WIDTH_HEADER_NAME);
						String cameraHeightStr = requestHeaders.get(CAMERA_HEIGHT_HEADER_NAME);
						String cameraWaitStr = requestHeaders.get(CAMERA_WAIT_HEADER_NAME);
						String cameraSnapName = requestHeaders.get(CAMERA_SNAP_NAME_HEADER_NAME);
						String timeBasedSnapName = requestHeaders.get(CAMERA_SNAP_TIME_BASED_NAME_HEADER_NAME);

						boolean timeBasedName = false;
						if (timeBasedSnapName == null) {
							timeBasedName = this.snapRequestManager.getSnapshotServer().getSnapThreadStatus().isTimeBaseSnapName();
						} else {
							timeBasedName = "true".equals(timeBasedSnapName.trim());
						}
						if (timeBasedName) {
							if (cameraSnapName == null) {
								cameraSnapName = "dynamic";
							} else {
								cameraSnapName += " (dynamic)";
							}
						}

						System.out.println("1 - Setting timeBasedName:" + timeBasedName + ", header:" + timeBasedSnapName);

						if (cameraRotStr != null) {
							snapThreadStatus.setRot(Integer.parseInt(cameraRotStr.trim()));
						}
						if (cameraWidthStr != null) {
							snapThreadStatus.setWidth(Integer.parseInt(cameraWidthStr.trim()));
						}
						if (cameraHeightStr != null) {
							snapThreadStatus.setHeight(Integer.parseInt(cameraHeightStr.trim()));
						}
						if (cameraWaitStr != null) {
							snapThreadStatus.setWait(Long.parseLong(cameraWaitStr.trim()));
						}
						if (cameraSnapName != null) {
							snapThreadStatus.setSnapName(cameraSnapName.trim());
						}
						System.out.println("2 - Setting timeBasedName:" + timeBasedName + ", header:" + timeBasedSnapName);
						snapThreadStatus.setTimeBaseSnapName(timeBasedName);
					}
				} else {
					String errMess = "SNAP-0003: No Snap Status found.";
					String content = new Gson().toJson(errMess);
					response.setStatus(Response.BAD_REQUEST);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
					return response;
				}
				this.snapRequestManager.getSnapshotServer().startSnapThread(snapThreadStatus); // Thread starts here.
			} catch (Exception ex) {

				ex.printStackTrace();

				String content = new Gson().toJson(ex);
				response.setStatus(Response.BAD_REQUEST);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
				return response;
			}
		} else if (config) {
			try {
				SnapSnapSnap.SnapStatus snapThreadStatus = null;
				try {
					snapThreadStatus = this.snapRequestManager.getSnapshotServer().getSnapThreadStatus();
				} catch (Exception ex) {
					String content = new Gson().toJson(ex);
					response.setStatus(Response.BAD_REQUEST);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
					return response;
				}
				Map<String, String> requestHeaders = request.getHeaders();
				if (snapThreadStatus != null) {
					if (requestHeaders != null) {
						String cameraRotStr = requestHeaders.get(CAMERA_ROT_HEADER_NAME);
						String cameraWidthStr = requestHeaders.get(CAMERA_WIDTH_HEADER_NAME);
						String cameraHeightStr = requestHeaders.get(CAMERA_HEIGHT_HEADER_NAME);
						String cameraWaitStr = requestHeaders.get(CAMERA_WAIT_HEADER_NAME);
						String cameraSnapName = requestHeaders.get(CAMERA_SNAP_NAME_HEADER_NAME);
						if (cameraRotStr != null) {
							snapThreadStatus.setRot(Integer.parseInt(cameraRotStr.trim()));
						}
						if (cameraWidthStr != null) {
							snapThreadStatus.setWidth(Integer.parseInt(cameraWidthStr.trim()));
						}
						if (cameraHeightStr != null) {
							snapThreadStatus.setHeight(Integer.parseInt(cameraHeightStr.trim()));
						}
						if (cameraWaitStr != null) {
							snapThreadStatus.setWait(Long.parseLong(cameraWaitStr.trim()));
						}
						if (cameraSnapName != null) {
							snapThreadStatus.setSnapName(cameraSnapName.trim());
						}
					}
				} else {
					String errMess = String.format("SNAP-0003: No Snap Status found.");
					String content = new Gson().toJson(errMess);
					response.setStatus(Response.BAD_REQUEST);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
					return response;
				}
				this.snapRequestManager.getSnapshotServer().setSnapThreadConfig(snapThreadStatus);
			} catch (Exception ex) {

				ex.printStackTrace();

				String content = new Gson().toJson(ex);
				response.setStatus(Response.BAD_REQUEST);
				RESTProcessorUtil.generateResponseHeaders(response, content.length());
				response.setPayload(content.getBytes());
				return response;
			}
		}
		try {
			SnapSnapSnap.SnapStatus snapThreadStatus = this.snapRequestManager.getSnapshotServer().getSnapThreadStatus();
			String content = new Gson().toJson(snapThreadStatus);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		} catch (Exception ex) {
			String content = new Gson().toJson(ex);
			response.setStatus(Response.BAD_REQUEST);
			RESTProcessorUtil.generateResponseHeaders(response, content.length());
			response.setPayload(content.getBytes());
			return response;
		}
	}

	/**
	 * Verb is GET
	 *
	 * @param request May contain QS params (see below in the code)
	 * @return the Response
	 */
	private Response getLastSnapshot(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> prms = request.getQueryStringParameters();

		if (verbose) {
			System.out.println("getLastSnapshot, Query String:");
			if (prms != null) {
				prms.keySet().forEach(key -> {
					System.out.println(String.format("%s=%s", key, prms.get(key)));
				});
			} else {
				System.out.println("getLastSnapshot, No QueryString");
			}
		}

		String fileName = SnapshotServer.snap == null ? null : SnapshotServer.snap.getLastSnapshotName(); // .snapshotName;
		String urlFullPath = fileName; // SnaphotServer.snapshotName;

		final List<String> supported = Arrays.asList(
			"gray", "blur", "threshold", "canny", "contours", "invert"
		);
		/* Managed parameters
		 * - gray, blur, threshold, canny, contours
		 * - like in ?gray=1&blur=5&canny=3
		 *
		 * If value < 1, ignored.
		 * All values > 0 are sorted, and executed in this order.
		 *
		 */
		final Map<Integer, String> transformations = new TreeMap<>((o1, o2) -> o1.compareTo(o2)); // smaller to greater
		if (prms != null && prms.size() > 0) {
			prms.keySet().forEach(k -> {
				if (verbose) {
					System.out.println(String.format("Managing SQ prm %s : %s", k, prms.get(k)));
				}
				if (supported.contains(k)) {
					try {
						int idx = Integer.parseInt(prms.get(k));
						if (idx > 0) {
							transformations.put(idx, k);
						}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				} else {
					System.err.println(String.format("Un-supported QS parameter [%s]", k));
				}
			});
		}

		if (transformations.size() > 0) {
			if (verbose) {
				transformations.keySet().forEach(rank -> {
					System.out.println(String.format("%d -> %s", rank, transformations.get(rank)));
				});
			}
			// Apply transformations here
			if ("true".equals(System.getProperty("with.opencv", "true"))) {
				try {
					Mat image = Imgcodecs.imread(SnapshotServer.snap.getLastSnapshotName());
					if (verbose) {
						System.out.println(String.format("Original image: w %d, h %d, %d channel(s)", image.width(), image.height(), image.channels()));
					}
					if (image.width() > 0 && image.height() > 0) {
						Mat finalMat = image;

						for (int rank : transformations.keySet()) {
//							System.out.println(String.format("%d -> %s", rank, transformations.get(rank)));
							String tx = transformations.get(rank);
							switch (tx) {
								case "gray":
									Mat gray = new Mat();
									Imgproc.cvtColor(finalMat, gray, Imgproc.COLOR_BGR2GRAY);
									finalMat = gray;
									break;
								case "blur":
									double sigmaX = 0d;
									final Size kSize = new Size(31, 31);
									Mat blurred = new Mat();
									Imgproc.GaussianBlur(finalMat, blurred, kSize, sigmaX);
									finalMat = blurred;
									break;
								case "threshold":
									Mat threshed = new Mat();
									Imgproc.threshold(finalMat, threshed, 127, 255, 0);
									finalMat = threshed;
									break;
								case "canny":
									Mat canny = new Mat();
									Imgproc.Canny(finalMat, canny, 10, 100);
									finalMat = canny;
									break;
								case "contours": // That one needs some love...
									List<MatOfPoint> contours = new ArrayList<>();
									Imgproc.findContours(finalMat, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
									Imgproc.drawContours(finalMat, contours, -1, new Scalar(0, 255, 0), 2);
									break;
								case "invert":
									Mat inverted = new Mat();
									Core.bitwise_not(finalMat, inverted);
									finalMat = inverted;
									break;
								default:
									break;
							}
						}
						fileName = SnapshotServer.txSnapshotName;
						urlFullPath = SnapshotServer.txSnapshotName;
						Imgcodecs.imwrite(fileName, finalMat);
					} else {
						// Empty!
						if (verbose) {
							System.out.println("Image is empty.");
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		SnapPayload payload = new SnapPayload()
				.status("Ok")
				.fullPath(fileName)
				.snapUrl(String.format("%s", urlFullPath));

		String content = new Gson().toJson(payload);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
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
