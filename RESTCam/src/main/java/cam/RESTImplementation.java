package cam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pi4j.io.i2c.I2CFactory;
import http.HTTPServer;
import http.HTTPServer.Operation;
import http.HTTPServer.Request;
import http.HTTPServer.Response;
import http.RESTProcessorUtil;
import i2c.servo.PCA9685;
import implementation.cam.CameraManager;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class defines the REST operations supported by the HTTP Server.
 * <p>
 * This list is defined in the <code>List&lt;Operation&gt;</code> named <code>operations</code>.
 * <br/>
 * Camera can be oriented by 2 servos, driven by a PCA9685.
 * Default tilt servo is #15
 * Default heading servo is #14
 * Can be overridden by -Dservo.heading=XX and -Dservo.tilt=XX
 * </p>
 */
public class RESTImplementation {

	private final static boolean verbose = "true".equals(System.getProperty("cam.verbose", "false"));

	private CamRequestManager camRequestManager;
	private final static String CAM_PREFIX = "/cam";

	private static boolean foundPCA9685 = true;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private static int freq = 60;

	private PCA9685 servoBoard = null;

	private int servoHeading = 14;
	private int servoTilt = 15;

	public RESTImplementation(CamRequestManager camRequestManager) {
		this.camRequestManager = camRequestManager;
		// Check duplicates in operation list. Barfs if duplicate is found.
		RESTProcessorUtil.checkDuplicateOperations(operations);

		try {
//		System.out.println("Driving Servos on Channels " + headingServoID + " and " + tiltServoID);
			this.servoBoard = new PCA9685();

			try {
				servoHeading = Integer.parseInt(System.getProperty("servo.heading", String.valueOf(servoHeading)));
			} catch (NumberFormatException nfe) {
				System.out.println(String.format("Defaulting Heading Servo to", servoHeading));
			}
			try {
				servoTilt = Integer.parseInt(System.getProperty("servo.tilt", String.valueOf(servoTilt)));
			} catch (NumberFormatException nfe) {
				System.out.println(String.format("Defaulting Tilt Servo to", servoTilt));
			}

			try {
				this.servoBoard.setPWMFreq(freq); // Set frequency in Hz
				// Set servos to Zero.
				setAngle(servoHeading, 0f);
				setAngle(servoTilt, 0f);
			} catch (NullPointerException npe) {
				foundPCA9685 = false;
				System.err.println("+------------------------------------------------------------");
				System.err.println("| (RESTCam) PCA9685 was NOT initialized.\n| Check your wiring, or make sure you are on a Raspberry Pi...");
				System.err.println("| Moving on anyway...");
				System.err.println("+------------------------------------------------------------");
			}
		} catch (I2CFactory.UnsupportedBusNumberException | UnsatisfiedLinkError usle) {
			foundPCA9685 = false;
			System.err.println("+---------------------------------------------------------------------");
			System.err.println("| You might not be on a Raspberry Pi, or PI4J/WiringPi is not there...");
			System.err.println("| Moving on anyway...");
			System.err.println("+---------------------------------------------------------------------");
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
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
					CAM_PREFIX + "/oplist",
					this::getOperationList,
					"List of all available operations, on cam request manager."),
			new Operation(
					"POST",
					CAM_PREFIX + "/snap",
					this::takeSnap,
					"Takes a snapshot. Optional Query String parameters are 'name', 'rot', 'width', 'height'."),
			new Operation(
					"GET",
							CAM_PREFIX + "/position",
							this::getCameraPosition,
					"When the camera can be oriented (2 servos), returns the tilt (up/down) and heading (left/right) values"),
			new Operation(
					"POST",
					CAM_PREFIX + "/tilt",
					this::setCameraTilt,
					"When the camera can be oriented (2 servos), sets the tilt value (up/down) [-90..90]"),
			new Operation(
					"POST",
					CAM_PREFIX + "/heading",
					this::setCameraHeading,
					"When the camera can be oriented (2 servos), sets the heading value (left/right) [-90..90]")
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
			request.setRequestPattern(op.getPath());        // To get the prms later on.
			Response processed = op.getFn().apply(request); // Execute here.
			return processed;
		} else {
			throw new UnsupportedOperationException(String.format("%s not managed", request.toString()));
		}
	}

	private Response getOperationList(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);

		List<Operation> opList = this.getOperations(); // Aggregates ops from all request managers
		String content = new Gson().toJson(opList);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());
		return response;
	}


	/**
	 * Take a snap
	 * @param request QS prms: 'rot', 'height', 'width', 'name'
	 * @return
	 */
	private Response takeSnap(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		Map<String, String> prms = request.getQueryStringParameters();
		// rot, width, height, name.
		int rot = 180;
		int width = 640;
		int height = 480;
		String name = "";
		if (prms != null && prms.get("rot") != null) {
			try {
				rot = Integer.parseInt(prms.get("rot"));
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SNAP-0001")
								.errorMessage(String.format("Bad 'rot' parameter [%s], must be an integer", prms.get("rot"))));
				return response;
			}
		}
		if (prms != null && prms.get("width") != null) {
			try {
				width = Integer.parseInt(prms.get("width"));
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SNAP-0002")
								.errorMessage(String.format("Bad 'width' parameter [%s], must be an integer", prms.get("width"))));
				return response;
			}
		}
		if (prms != null && prms.get("height") != null) {
			try {
				height = Integer.parseInt(prms.get("height"));
			} catch (NumberFormatException nfe) {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SNAP-0003")
								.errorMessage(String.format("Bad 'height' parameter [%s], must be an integer", prms.get("height"))));
				return response;
			}
		}
		if (prms != null && prms.get("name") != null) {
			name = prms.get("name");
		}
		String snapshotName = "";
		if (name.isEmpty()) {
			name = SDF.format(new Date());
		}
		try {
			snapshotName = CameraManager.snap(name, rot, width, height);
		} catch (Exception ex) {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SNAP-0004")
							.errorMessage(ex.toString()));
			return response;
		}
		SnapPayload payload = new SnapPayload()
				.status("Ok")
				.fullPath(snapshotName)
				.snapUrl(String.format("%s.jpg", name));

		String content = new Gson().toJson(payload);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	/**
	 * Get the camera position, tilt and heading.
	 *
	 * @param request
	 * @return
	 */
	private Response getCameraPosition(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		if (!foundPCA9685) {
			response = HTTPServer.buildErrorResponse(response,
					Response.NOT_FOUND,
					new HTTPServer.ErrorPayload()
							.errorCode("SNAP-0101")
							.errorMessage("Board PCA9685 was not found"));
			return response;
		}
		int tilt = CameraManager.getTilt();
		int heading = CameraManager.getHeading();
		CameraPosition position = new CameraPosition()
				.heading(heading)
				.tilt(tilt);

		String content = new Gson().toJson(position);
		RESTProcessorUtil.generateResponseHeaders(response, content.length());
		response.setPayload(content.getBytes());

		return response;
	}

	/**
	 * Set the camera tilt position (up / down).
	 * Requires a payload like { "tilt": 10 }
	 *
	 * @param request
	 * @return
	 */
	private Response setCameraTilt(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		if (!foundPCA9685) {
			if (verbose) {
				System.out.println("No PCA9685 was found");
			}
			response = HTTPServer.buildErrorResponse(response,
					Response.NOT_FOUND,
					new HTTPServer.ErrorPayload()
							.errorCode("SNAP-0101")
							.errorMessage("Board PCA9685 was not found"));
			return response;
		}
		CameraPosition pos = null;

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				if (verbose) {
					System.out.println(String.format("setCameraTilt: %s", payload));
				}
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					pos = gson.fromJson(stringReader, CameraPosition.class);
					int tilt = pos.tilt;
					setTiltServoAngle(tilt);
					CameraManager.setTilt(tilt); // Here
					tilt = CameraManager.getTilt();
					int heading = CameraManager.getHeading();
					CameraPosition position = new CameraPosition()
							.heading(heading)
							.tilt(tilt);

					String content = new Gson().toJson(position);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("SNAP-0201")
									.errorMessage(ex.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SNAP-0202")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SNAP-0202")
							.errorMessage("Request payload not found"));
			return response;
		}

		return response;
	}

	/**
	 * Set the camera heading position (left / right).
	 * Requires a payload like { "heading": 10 }
	 *
	 * @param request
	 * @return
	 */
	private Response setCameraHeading(Request request) {
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
		if (!foundPCA9685) {
			if (verbose) {
				System.out.println("No PCA9685 was found");
			}
			response = HTTPServer.buildErrorResponse(response,
					Response.NOT_FOUND,
					new HTTPServer.ErrorPayload()
							.errorCode("SNAP-0101")
							.errorMessage("Board PCA9685 was not found"));
			return response;
		}
		CameraPosition pos = null;

		if (request.getContent() != null && request.getContent().length > 0) {
			String payload = new String(request.getContent());
			if (!"null".equals(payload)) {
				if (verbose) {
					System.out.println(String.format("setCameraHeading: %s", payload));
				}
				Gson gson = new GsonBuilder().create();
				StringReader stringReader = new StringReader(payload);
				try {
					pos = gson.fromJson(stringReader, CameraPosition.class);
					int heading = pos.heading;
					setHeadingServoAngle(heading);
					CameraManager.setHeading(heading); // Here
					int tilt = CameraManager.getTilt();
					heading = CameraManager.getHeading();
					CameraPosition position = new CameraPosition()
							.heading(heading)
							.tilt(tilt);

					String content = new Gson().toJson(position);
					RESTProcessorUtil.generateResponseHeaders(response, content.length());
					response.setPayload(content.getBytes());
				} catch (Exception ex) {
					response = HTTPServer.buildErrorResponse(response,
							Response.BAD_REQUEST,
							new HTTPServer.ErrorPayload()
									.errorCode("SNAP-0201")
									.errorMessage(ex.toString()));
					return response;
				}
			} else {
				response = HTTPServer.buildErrorResponse(response,
						Response.BAD_REQUEST,
						new HTTPServer.ErrorPayload()
								.errorCode("SNAP-0202")
								.errorMessage("Request payload not found"));
				return response;
			}
		} else {
			response = HTTPServer.buildErrorResponse(response,
					Response.BAD_REQUEST,
					new HTTPServer.ErrorPayload()
							.errorCode("SNAP-0202")
							.errorMessage("Request payload not found"));
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
		Response response = new Response(request.getProtocol(), Response.STATUS_OK);
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

	public static class CameraPosition {
		int tilt;
		int heading;

		public CameraPosition tilt(int tilt) {
			this.tilt = tilt;
			return this;
		}

		public CameraPosition heading(int heading) {
			this.heading = heading;
			return this;
		}
	}

	/*
	 * Servo (pan-tilt) methods.
	 * Could be isolated in another class, along with all the PCA9685 related variables...
	 */
	public void setHeadingServoAngle(final float f) {
		setAngle(servoHeading, f);
	}
	public void setTiltServoAngle(final float f) {
		setAngle(servoTilt, f);
	}

	public void stopHeadingServo() {
		System.out.println(">> Stopping heading servo");
		stop(servoHeading);
	}

	public void stopTiltServo() {
		System.out.println(">> Stopping tilt servo");
		stop(servoTilt);
	}

	private void stop(int servo) { // Set to 0
		if (foundPCA9685) {
			this.servoBoard.setPWM(servo, 0, 0);
		}
	}

	private void setAngle(int servo, float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);

		String mess = String.format("Servo %d, angle %.02f\272, pwm: %d", servo, f, pwm);
		System.out.println(mess);

		try {
			if (foundPCA9685) {
				servoBoard.setPWM(servo, 0, pwm);
			} else {
				System.out.println("No Servo board.");
			}
		} catch (IllegalArgumentException iae) {
			System.err.println(String.format("Cannot set servo %d to PWM %d", servo, pwm));
			iae.printStackTrace();
		}
	}

	/*
	 * deg in [-90..90]
	 */
	private static int degreeToPWM(int min, int max, float deg) {
		int diff = max - min;
		float oneDeg = diff / 180f;
		return Math.round(min + ((deg + 90) * oneDeg));
	}
}
