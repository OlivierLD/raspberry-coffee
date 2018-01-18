package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static utils.StaticUtil.userInput;

/*
 * Two servos - one standard, one continous
 * Enter all the values from the command line, and see for yourself.
 */
public class InteractiveServo {
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

		String servoChannel = userInput("Servo Channel (0-15) : ");
		int servo = 0;
		try {
			servo = Integer.parseInt(servoChannel);
			if (servo < 0 || servo > 15) {
				System.out.println("Must be between 0 and 15, exiting");
				System.exit(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		boolean keepGoing = true;
		System.out.println("Enter 'quit' to exit.");
		while (keepGoing) {
			String s1 = userInput("pulse width in ticks  (0..4095) ? > ");
			if ("QUIT".equalsIgnoreCase(s1)) {
				keepGoing = false;
			} else {
				try {
					int on = Integer.parseInt(s1);
					if (on < 0 || on > 4_095) {
						System.out.println("Values between 0 and 4095.");
					} else {
						System.out.println("setPWM(" + servo + ", 0, " + on + ");");
						servoBoard.setPWM(servo, 0, on);
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
