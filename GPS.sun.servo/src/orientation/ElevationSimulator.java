package orientation;

import calculation.AstroComputer;
import calculation.SightReductionUtil;
import i2c.sensor.LSM303;
import i2c.sensor.listener.LSM303Listener;
import i2c.servo.pwm.PCA9685;
import java.util.Calendar;
import java.util.TimeZone;
import user.util.GeomUtil;

/**
 * System variables:
 *
 * latitude -Dlatitude=37.7489
 * longitude -Dlongitude=-122.5070
 * declination -Ddeclination=14
 * tolerance -Dtolerance=5
 *
 * or GPS... (later).
 */
public class ElevationSimulator {

	private static double declination = 14D; // E+, W-
	private static int targetWindow = 5;

	private static double latitude = 0D;
	private static double longitude = 0D;

	private static boolean keepWorking = true;

	private static double he = 0D, z = 0D;
	private static boolean dayTime = true;

	private static boolean orientationVerbose = false;
	private static boolean astroVerbose = false;

	private static void getSunData(double lat, double lng) {
		Calendar current = Calendar.getInstance(TimeZone.getTimeZone("etc/UTC"));
		AstroComputer.setDateTime(current.get(Calendar.YEAR),
						current.get(Calendar.MONTH) + 1,
						current.get(Calendar.DAY_OF_MONTH),
						current.get(Calendar.HOUR_OF_DAY),
						current.get(Calendar.MINUTE),
						current.get(Calendar.SECOND));
		AstroComputer.calculate();
		SightReductionUtil sru = new SightReductionUtil(AstroComputer.getSunGHA(),
						AstroComputer.getSunDecl(),
						lat,
						lng);
		sru.calculate();
		he = sru.getHe().doubleValue();
		z = sru.getZ().doubleValue();
	}

	private int servo = -1;

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private int servoMin = DEFAULT_SERVO_MIN;
	private int servoMax = DEFAULT_SERVO_MAX;
	private int diff = servoMax - servoMin;
	private static int freq = 60;

	private PCA9685 servoBoard = null;

	public ElevationSimulator(int channel) {
		try {
			System.out.println("Driving Servo on Channel " + channel);
			this.servo = channel;
			this.servoBoard = new PCA9685();
			this.servoBoard.setPWMFreq(freq); // Set frequency in Hz

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		final ElevationSimulator instance = this;
		Thread timeThread = new Thread(() -> {
			while (keepWorking) {
				getSunData(latitude, longitude);
				if (he > 0) {
					dayTime = true;
					if (astroVerbose) {
						System.out.println(String.format("From %s / %s, He:%.02f\272, Z:%.02f\272 (true)",
										GeomUtil.decToSex(latitude, GeomUtil.SWING, GeomUtil.NS),
										GeomUtil.decToSex(longitude, GeomUtil.SWING, GeomUtil.EW),
										he,
										z));
					}
					instance.setAngle((float)Math.round(he));
				} else {
					dayTime = false;
					System.out.println("Fait nuit...");
				}
				try { Thread.sleep(1_000L); } catch (Exception ex) {}
			}
			System.out.println("Timer done.");
		});
		System.out.println("Starting the timer loop");
		this.stop();
		timeThread.start();
	}

	public void setAngle(float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		// System.out.println(f + " degrees (" + pwm + ")");
		servoBoard.setPWM(servo, 0, pwm);
	}

	public void setPWM(int pwm) {
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
		astroVerbose = "true".equals(System.getProperty("astro.verbose", "false"));

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
		String strTol = System.getProperty("tolerance", "5");
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

		new ElevationSimulator(channel);
	}
}
