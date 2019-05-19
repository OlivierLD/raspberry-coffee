package orientation;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

import static utils.StaticUtil.userInput;

/**
 * Used to calibrate/check the servos angles.
 * Takes the servo angles from the CLI
 */
public class InteractiveServoTester {

	private final static int DEFAULT_SERVO_MIN = 122; // Value for Min position (-90, unit is [0..1023])
	private final static int DEFAULT_SERVO_MAX = 615; // Value for Max position (+90, unit is [0..1023])

	private static int servoMin = DEFAULT_SERVO_MIN;
	private static int servoMax = DEFAULT_SERVO_MAX;
	private static int freq = 60;

	private static PCA9685 servoBoard;

	public static void main(String... args) {

		int servoHeading = 14;
		int servoTilt = 15;

		// Supported parameters --heading:14 --tilt:15
		if (args.length > 0) {
			for (String prm : args) {
				if (prm.startsWith("--heading:")) {
					try {
						servoHeading = Integer.parseInt(prm.substring("--heading:".length()));
					} catch (Exception e) {
						throw e;
					}
				} else if (prm.startsWith("--tilt:")) {
					try {
						servoTilt = Integer.parseInt(prm.substring("--tilt:".length()));
					} catch (Exception e) {
						throw e;
					}
				}
			}
		}

		try {
//		System.out.println("Driving Servos on Channels " + headingServoID + " and " + tiltServoID);
			servoBoard = new PCA9685();
			try {
				servoBoard.setPWMFreq(freq); // Set frequency in Hz
			} catch (Exception npe) {
//		} catch (NullPointerException npe) {
				System.err.println("+------------------------------------------------------------");
				System.err.println("| (InteractiveServoTester) PCA9685 was NOT initialized.\n| Check your wiring, or make sure you are on a Raspberry Pi...");
				System.err.println("| Moving on anyway...");
				System.err.println("+------------------------------------------------------------");
			}
		} catch (UnsatisfiedLinkError | I2CFactory.UnsupportedBusNumberException oops) {
			System.err.println("+---------------------------------------------------------------------");
			System.err.println("| You might not be on a Raspberry Pi, or PI4J/WiringPi is not there...");
			System.out.println("| Or you don't have enough credentials (sudo?).");
			System.err.println("| Moving on anyway...");
			System.err.println("+---------------------------------------------------------------------");
			System.err.println("Here is the stack, for info:");
			oops.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		int servo = -1;
		float servoAngle = 0f;
		boolean read = true;
		System.out.println("Enter 'Q' at the prompt to quit.");
		while (read) {
			while (servo == -1) {
				String servoStr = userInput("H for heading, T for tilt > ");
				switch (servoStr) {
					case "T":
					case "t":
						servo = servoTilt;
						break;
					case "H":
					case "h":
						servo = servoHeading;
						break;
					default:
						break;
				}
			}
//		setAngle(servo, 0f); // init position
			String input = userInput(String.format("Enter servo's angle [-90..90], now %.02f > ", servoAngle));
			if ("Q".equalsIgnoreCase(input.trim())) {
				read = false;
			} else {
				try {
					float newAngle = Float.parseFloat(input);
					if (newAngle < -90 || newAngle > 90) {
						System.out.println("Bad range [-90..90]");
					} else {
						servoAngle = newAngle;
						setAngle(servo, servoAngle);
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
		System.out.println("Bye.");

		setAngle(servo, 0f);
		stop(servo); // Release
		if (servoBoard != null) {
			servoBoard.close();
		}

		try {
			Thread.sleep(1_000L);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
		}
	}

	private static int degreeToPWM(int min, int max, float deg) {
		int diff = max - min;
		float oneDeg = diff / 180f;
		return Math.round(min + ((deg + 90) * oneDeg)); // -90..90
	}

	private static void setAngle(int servo, float f) {
		int pwm = degreeToPWM(servoMin, servoMax, f);
		try {
			if (servoBoard != null) {
				System.out.println(String.format(">> Setting pwm = %d, for angle %f (min %d, max %d)", pwm, f, servoMin, servoMax));
				servoBoard.setPWM(servo, 0, pwm);
			} else {
				System.out.println(String.format("Simulating pwm = %d", pwm));
			}
		} catch (IllegalArgumentException iae) {
			System.err.println(String.format("Cannot set servo %d to PWM %d", servo, pwm));
			iae.printStackTrace();
		}
	}

	private static void stop(int servo) { // Set to 0, and release
		servoBoard.setPWM(servo, 0, 0);
	}
}
