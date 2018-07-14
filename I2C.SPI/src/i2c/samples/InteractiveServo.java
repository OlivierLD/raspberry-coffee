package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.pwm.PCA9685;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static utils.StaticUtil.userInput;

/**
 * Enter all the values from the command line, and see for yourself.
 *
 * Note:
 * This class can be used to calibrate your servos before using them.
 * Servos theoretically use PWM from 1 to 2ms, but it is not unusual
 * to get servos using PWM from 0.5 to 2.5ms.
 *
 * This class can help to find the right calibration.
 * Look at your servo, and enter the PWM values. Then see where the servo goes (standard servo).
 * You need to find the extrema...
 *
 * Example:
$> ./inter.servo
Connected to bus. OK.
Connected to device. OK.
freq (40-1000)  ? > 60
Setting PWM frequency to 60 Hz
Estimated pre-scale: 100.72526
Final pre-scale: 101.0
Servo Channel (0-15) : 1
Entry method: T for Ticks (0..4095), P for Pulse (in ms) > p
Enter 'quit' to exit.
Pulse in ms > 1.5
setServoPulse(1, 1.5)
4.069010416666667 ?s per bit, pulse:368
-------------------
Pulse in ms > 0.5
setServoPulse(1, 0.5)
4.069010416666667 ?s per bit, pulse:122
-------------------
Pulse in ms > 0.6
setServoPulse(1, 0.6)
4.069010416666667 ?s per bit, pulse:147
-------------------
Pulse in ms > 2.4
setServoPulse(1, 2.4)
4.069010416666667 ?s per bit, pulse:589
-------------------
Pulse in ms > 2.5
setServoPulse(1, 2.5)
4.069010416666667 ?s per bit, pulse:614
-------------------
Pulse in ms > 2.6
setServoPulse(1, 2.6)
4.069010416666667 ?s per bit, pulse:638
-------------------
Pulse in ms > 2.7
setServoPulse(1, 2.7)
4.069010416666667 ?s per bit, pulse:663
-------------------
 ...etc.
 *
 * In the session above, at 60 Hz, extrema values turned out to be 0.52 - 2.5, translated into 127 - 614.
 *
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

		String entryMethod = userInput("Entry method: T for Ticks (0..4095), P for Pulse (in ms) > ");
		boolean keepGoing = true;
		System.out.println("Enter 'quit' to exit.");
		while (keepGoing) {
			String s1 = userInput("T".equalsIgnoreCase(entryMethod) ? "Pulse width in ticks (0..4095) ? > " : "Pulse in ms > ");
			if ("QUIT".equalsIgnoreCase(s1)) {
				keepGoing = false;
			} else {
				try {
					if ("T".equalsIgnoreCase(entryMethod)) {
						int on = Integer.parseInt(s1);
						if (on < 0 || on > 4_095) {
							System.out.println("Values between 0 and 4095.");
						} else {
							System.out.println("setPWM(" + servo + ", 0, " + on + ");");
							servoBoard.setPWM(servo, 0, on);
							System.out.println("-------------------");
						}
					} else {
						float pulseMS = Float.parseFloat(s1);
						if (pulseMS < 0) {
							System.out.println("Pulse must be positive.");
						} else {
							System.out.println("setServoPulse(" + servo + ", " + pulseMS + ")");
							servoBoard.setServoPulse(servo, pulseMS);
							System.out.println("-------------------");
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		System.out.println("Done.");
	}
}
