package orientation;

import i2c.sensor.LSM303;
import i2c.sensor.listener.LSM303Listener;
import i2c.servo.pwm.PCA9685;
import user.util.GeomUtil;

/**
 * The job of this one is to point North, and keep pointing to it.
 * You have an LSM303 attached to a standard servo.
 * Start at Zero, point the x -> arrow drawn on the LSM303 as close to the north as you can, and
 * start the program.
 *
 * Then move the servo (to which the board is attached).
 * The servo should orient the board, so it keeps pointing in the same (North, aka 0) direction.
 *
 * <p>
 *   <b>This is a test, the goal here is to keep pointing North!</b>
 * </p>
 *
 * System variables:
 *
 * latitude -Dlatitude=37.7489
 * longitude -Dlongitude=-122.5070
 * declination -Ddeclination=14
 * tolerance -Dtolerance=1
 *
 * or GPS... (later).
 */
public class Orientation101 {

	private static double declination = 14D; // E+, W-
	private static int targetWindow = 1;

	private static double latitude = 0D;
	private static double longitude = 0D;

	private static double z = 0D;

	private static boolean orientationVerbose = false;

	private int servo = -1;
	private static int currentServoAngle = 0;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;
	private static int freq = 60;

	private PCA9685 servoBoard = null;

	public Orientation101(int channel) {
		try {
			System.out.println("Driving Servo on Channel " + channel);
			this.servo = channel;
			this.servoBoard = new PCA9685();
			this.servoBoard.setPWMFreq(freq); // Set frequency in Hz

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

	}

	public void setAngle(float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		// System.out.println(f + " degrees (" + pwm + ")");
		servoBoard.setPWM(servo, 0, pwm);
	}

	public void stop() { // Set to 0
		servoBoard.setPWM(servo, 0, 0);
	}

	/*
	 * deg in [-90..90]
	 */
	private static int degreeToPWM(int min, int max, float deg) {
		int diff = max - min;
		float oneDeg = diff / 180f;
		return Math.round(min + ((deg + 90) * oneDeg));
	}

	public static void main(String... args) {

		int channel = 14;
		if (args.length > 0) {
			try {
				channel = Integer.parseInt(args[0]);
			} catch (Exception e) {
				throw e;
			}
		}

		orientationVerbose = "true".equals(System.getProperty("orient.verbose", "false"));

		String strLat = System.getProperty("latitude");
		if (strLat != null) {
			try {
				latitude = Double.parseDouble(strLat);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strLong = System.getProperty("longitude");
		if (strLong != null) {
			try {
				longitude = Double.parseDouble(strLong);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strDec = System.getProperty("declination");
		if (strDec != null) {
			try {
				declination = Double.parseDouble(strDec);
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}
		String strTol = System.getProperty("tolerance", "1");
		if (strTol != null) {
			try {
				targetWindow = Integer.parseInt(strTol);
				if (targetWindow < 1) {
					throw new IllegalArgumentException("Tolerance must be an int, greater than 1");
				}
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.exit(1);
			}
		}

		System.out.println("----------------------------------------------");
		System.out.println(String.format("Position %s / %s, Mag Decl. %.01f, tolerance %d\272 each way.",
						GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
						GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW),
						declination,
						targetWindow));
		System.out.println("----------------------------------------------");

		final LSM303 sensor;
		final LSM303Listener orientationListener;

		final Orientation101 instance = new Orientation101(channel);
		// Set to 0
		instance.setAngle((float)currentServoAngle);

		try {
			sensor = new LSM303();
			orientationListener = new LSM303Listener() {
				@Override
				public void dataDetected(float accX, float accY, float accZ, float magX, float magY, float magZ, float heading, float pitch, float roll) {
					// Heading
					double headingDiff = z - (heading + declination);
					if (headingDiff < -180) {
						headingDiff += 360D;
					}
					String headingMessage = "Heading OK";
					int delta = 0;
					if (headingDiff > targetWindow) {
						headingMessage = "Turn right";
						delta = -1; 
					} else if (headingDiff < -targetWindow) {
						headingMessage = "Turn left";
						delta = 1;
					}
					if (orientationVerbose) {
						System.out.println(String.format("Board orientation: Heading %.01f, Target Z: %.01f, %s", heading, z, headingMessage));
					}
					// Drive servo accordingly, to point north.
					if (delta != 0) {						
						currentServoAngle += delta;
//					System.out.println("Pointing to " + currentServoAngle);
						instance.setAngle((float)currentServoAngle);
					}
				}

				@Override
				public void close() {
					super.close();
				}
			};
			sensor.setDataListener(orientationListener);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\nBye.");
				synchronized (sensor) {
					sensor.setKeepReading(false);
					orientationListener.close();
					instance.setAngle(0f);
					try {
						Thread.sleep(1_500L);
					} catch (InterruptedException ie) {
						System.err.println(ie.getMessage());
					}
				}
			}));

			System.out.println("Start listening to the LSM303");
			sensor.startReading();

		} catch (Throwable ex) {
			System.err.println(">>> OrientationDetector... <<< BAM!");
			ex.printStackTrace();
//		System.exit(1);
		}
	}
}
