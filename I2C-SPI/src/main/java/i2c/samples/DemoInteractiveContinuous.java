package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;
import utils.StaticUtil;
import utils.TCPUtils;

import static utils.TimeUtil.delay;

/**
 * <h3>For PCA9685</h3>
 * Continuous, interactive demo.
 * <br/>
 * Servos are analog devices... They may vary.
 * Amplitude (full speed backward to full speed forward) seems to be around 70. (Note: 4096 / 60 ~= 68 ?)
 * <br/>
 * <ul>
 * <li>First: find the middle position: servo is stopped. About 360 (for a parallax continuous). A Micro continuous servo could be 375.</li>
 * <li>Min is usually middle - (70 / 2)</li>
 * <li>Max is usually middle + (70 / 2)</li>
 * </ul>
 * This code will allow you to calibrate the servo.
 * <br/>
 * Also see {@link PCA9685} for more details.
 * <br/>
 * Note: This DOES NOT 100% work as documented..., analog devices.
 */
public class DemoInteractiveContinuous {
	public static void main(String... args) throws /*I2CFactory.UnsupportedBusNumberException*/ Exception {

		int argChannel = -1;
		if (args.length > 0) {
			// First arg is channel #
			argChannel = Integer.parseInt(args[0]);
			if (argChannel > 15 || argChannel < 0) {
				throw new IllegalArgumentException("Channel in [0..15] please!");
			}
		}
		PCA9685 servoBoard = null;
		boolean simulating = false;
		int freq = 60; // in [40..1000] . See below the relation between this and the rest of the world.

		try {
			servoBoard = new PCA9685();
			String freqSysVar = System.getProperty("servo.freq");
			if (freqSysVar != null) {
				try {
					freq = Integer.parseInt(freqSysVar);
					if (freq < 40 || freq > 1000) {
						System.err.println(String.format("Bad Value %d, should be in [40..1000]", freq));
						freq = 60;
					}
					System.out.println(String.format("Frequency now set to %d Hz", freq));
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
			servoBoard.setPWMFreq(freq); // Set frequency in Hz
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			simulating = true;
		}

		// Display default theoretical values
		System.out.println(String.format("Center: %04d, Min: %04d, Max: %04d", PCA9685.getServoCenterValue(freq), PCA9685.getServoMinValue(freq), PCA9685.getServoMaxValue(freq)));

		System.out.println("System data:");
		try {
			System.out.println(String.format("\tCore Voltage %s", TCPUtils.getCoreVoltage()));
			System.out.println(String.format("\tCPU Temp %s", TCPUtils.getCPUTemperature()));
			System.out.println(String.format("\tCPU Load %s", TCPUtils.getCPULoad()));
		} catch (Exception ex) {
			throw ex;
		}

		final int CONTINUOUS_SERVO_CHANNEL = (argChannel != -1) ? argChannel : 14;
//  final int STANDARD_SERVO_CHANNEL   = 15;

		int servo = CONTINUOUS_SERVO_CHANNEL;
		int servoMin     = 340;
		int servoStopsAt = 375;
		int servoMax     = 410;

		System.out.println(String.format("Servo #%d, [%d..%d].", CONTINUOUS_SERVO_CHANNEL, servoMin, servoMax));

		if (!simulating && servoBoard != null) {
			servoBoard.setPWM(servo, 0, 0);   // Stop the servo
			delay(2_000L);
			System.out.println(String.format("Let's go. Enter values in ~[%d..%d], middle: %d, 'S' to stop the servo, 'Q' to quit.", servoMin, servoMax, servoStopsAt));

			boolean keepLooping = true;
			while (keepLooping) {
				String userInput = StaticUtil.userInput("You say: > ");
				if (userInput.equalsIgnoreCase("Q")) {
					keepLooping = false;
				} else if (userInput.equalsIgnoreCase("S")) {
					servoBoard.setPWM(servo, 0, 0);   // Stop the servo
				} else {
					try {
						int pwmValue = Integer.parseInt(userInput);
//					if (pwmValue < servoMin) {
//						System.err.println(String.format("Bad value, min is %d (%d)", servoMin, pwmValue));
//					} else if (pwmValue > servoMax) {
//						System.err.println(String.format("Bad value, max is %d (%d)", servoMax, pwmValue));
//					} else {
						System.out.println(String.format("From value: %04d, pulse is %.03f", pwmValue, PCA9685.getPulseFromValue(freq, pwmValue)));
						servoBoard.setPWM(servo, 0, pwmValue); // Do it!
//					}
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
				}
			}
			servoBoard.setPWM(servo, 0, 0);   // Stop the servo
		} else {
			System.out.println("Go on a Pi...");
		}
		System.out.println("Done, bye.");
	}
}
