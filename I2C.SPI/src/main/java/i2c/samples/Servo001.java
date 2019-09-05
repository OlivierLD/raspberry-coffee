package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

import static utils.StaticUtil.userInput;

/*
 * Two servos - one standard, one continuous
 * Enter all the values from the command line, and see for yourself.
 */
public class Servo001 {
	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		PCA9685 servoBoard = new PCA9685();
		int freq = 60;
		String sFreq = userInput("freq (40-1000)  ? > ");
		try {
			freq = Integer.parseInt(sFreq);
		} catch (NumberFormatException nfe) {
			System.err.println("Defaulting freq to 60");
			nfe.printStackTrace();
		}
		if (freq < 40 || freq > 1_000) {
			throw new IllegalArgumentException("Freq only between 40 and 1000.");
		}
		servoBoard.setPWMFreq(freq); // Set frequency in Hz

		final int CONTINUOUS_SERVO_CHANNEL = 14;
		final int STANDARD_SERVO_CHANNEL = 15;

		int servo = STANDARD_SERVO_CHANNEL;

		String sServo = userInput("Servo: Continuous [C], Standard [S] > ");
		if ("C".equalsIgnoreCase(sServo)) {
			servo = CONTINUOUS_SERVO_CHANNEL;
		} else if ("S".equalsIgnoreCase(sServo)) {
			servo = STANDARD_SERVO_CHANNEL;
		} else {
			System.out.println("Only C or S... Defaulting to Standard.");
		}
		boolean keepGoing = true;
		System.out.println("Enter 'quit' to exit.");
		while (keepGoing) {
			String s1 = userInput("on  (0..4095) ? > ");
			if ("QUIT".equalsIgnoreCase(s1)) {
				keepGoing = false;
			} else {
				try {
					int on = Integer.parseInt(s1);
					String s2 = userInput("off (0..4095) ? > ");
					int off = Integer.parseInt(s2);
					if (on < 0 || on > 4_095 || off < 0 || off > 4_095) {
						System.out.println("Values between 0 and 4095.");
					} else if (off < on) {
						System.out.println("Off is lower than On...");
					} else {
						System.out.println("setPWM(" + servo + ", " + on + ", " + off + ");");
						servoBoard.setPWM(servo, on, off);
						System.out.println("-------------------");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		System.out.println("Done.");
	}
}
