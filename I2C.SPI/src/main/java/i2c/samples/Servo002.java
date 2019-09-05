package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

import static utils.StaticUtil.userInput;

/*
 * Standard servo
 * TowerPro SG-5010
 *
 * Enter the angle interactively, and see for yourself.
 */
public class Servo002 {
	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		PCA9685 servoBoard = new PCA9685();
		int freq = 60;
		servoBoard.setPWMFreq(freq); // Set frequency in Hz

		// For the TowerPro SG-5010
		int servoMin = 130;   // -90 deg
		int servoMax = 615;   // +90 deg

		final int STANDARD_SERVO_CHANNEL = 15;

		int servo = STANDARD_SERVO_CHANNEL;

		boolean keepGoing = true;
		System.out.println("[" + servoMin + ", " + servoMax + "]");
		System.out.println("Enter 'quit' to exit.");
		while (keepGoing) {
			String s1 = userInput("Angle in degrees (0: middle, -90: full left, 90: full right) ? > ");
			if ("QUIT".equalsIgnoreCase(s1)) {
				keepGoing = false;
			} else {
				try {
					int angle = Integer.parseInt(s1);
					if (angle < -90 || angle > 90) {
						System.err.println("Between -90 and 90 only");
					} else {
						int on = 0;
						int off = (int) (servoMin + (((double) (angle + 90) / 180d) * (servoMax - servoMin)));
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
