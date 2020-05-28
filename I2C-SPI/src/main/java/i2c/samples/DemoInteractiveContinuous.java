package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;
import utils.StaticUtil;
import utils.SystemUtils;
import utils.TimeUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private static void displayHelp() {
		System.out.println("Commands are:");
		System.out.println("\tS to Stop");
		System.out.println("\tQ to Quit");
		System.out.println("\tTH to Display theoretical values");
		System.out.println("\tXXX to set the pwmValue on the servo");
		System.out.println("\t[XXX:YYY] to go with pwmValues from XXX to YYY");
		System.out.println("\t[XXX-SS.S] apply pwmValue XXX to the servo for SS second(s).");
		System.out.println("\tPulse XXX to get the pulse corresponding to XXX");
	}

	private final static String CHANNEL_PREFIX = "--channel:";
	private final static String FREQUENCY_PREFIX = "--freq:";
	private final static int DEFAULT_CHANNEL = 14;

	private final static String PATTERN_1_STR = "^\\[\\d+:\\d+\\]$";
	private final static String PATTERN_2_STR = "^\\[\\d+-\\d+\\.*\\d*\\]$";

	public static void main(String... args) throws /*I2CFactory.UnsupportedBusNumberException*/ Exception {

		System.out.println("Supported program arguments: --channel:14 --freq:60");
		System.out.println("Values above are default values.");

		int argChannel = -1;
		int freq = 60; // in [40..1000] . See below the relation between this and the rest of the world.

		if (args.length > 0) {
			for (String arg : args) {
				if (arg.startsWith(CHANNEL_PREFIX)) {
					// First arg is channel #
					argChannel = Integer.parseInt(arg.substring(CHANNEL_PREFIX.length()));
					if (argChannel > 15 || argChannel < 0) {
						throw new IllegalArgumentException("Channel in [0..15] please!");
					}
				} else if (arg.startsWith(FREQUENCY_PREFIX)) {
					// First arg is channel #
					freq = Integer.parseInt(arg.substring(FREQUENCY_PREFIX.length()));
					if (freq > 1_000 || freq < 40) {
						throw new IllegalArgumentException("Frequency in [40..1000] please!");
					}
				}
			}
		}
		PCA9685 servoBoard = null;
		boolean simulating = false;

		try {
			servoBoard = new PCA9685();
			servoBoard.setPWMFreq(freq); // Set frequency in Hz
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			simulating = true;
		}

		// Display default theoretical values
		System.out.println(String.format("Theoretical values: Min: %04d, Center: %04d, Max: %04d", PCA9685.getServoMinValue(freq), PCA9685.getServoCenterValue(freq), PCA9685.getServoMaxValue(freq)));

		System.out.println("System data:");
		try {
			System.out.println(String.format("\tCore Voltage %s", SystemUtils.getCoreVoltage()));
			System.out.println(String.format("\tCPU Temp %s", SystemUtils.getCPUTemperature()));
			System.out.println(String.format("\tCPU Load %s", SystemUtils.getCPULoad()));
		} catch (Exception ex) {
			throw ex;
		}

		int servoChannel = (argChannel != -1) ? argChannel : DEFAULT_CHANNEL;

		System.out.println("----------------------------");
		System.out.println(String.format("Servo #%d, frequency %d Hz.", servoChannel, freq));
		System.out.println("----------------------------");

		Pattern patternOne = Pattern.compile(PATTERN_1_STR);
		Pattern patternTwo = Pattern.compile(PATTERN_2_STR);
		Matcher matcher = null;

		if (!simulating && servoBoard != null) {
			servoBoard.setPWM(servoChannel, 0, 0);   // Stop the servo
			delay(2_000L);
			System.out.println(String.format("Let's go. Enter 'S' to stop the servo, 'Q' to quit."));
			displayHelp();

			boolean keepLooping = true;
			while (keepLooping) {
				String userInput = StaticUtil.userInput("You say: > ");
				if (userInput.trim().isEmpty()) {
					displayHelp();
				} else if (userInput.equalsIgnoreCase("Q")) {
					keepLooping = false;
				} else if (userInput.equalsIgnoreCase("S")) {
					servoBoard.setPWM(servoChannel, 0, 0);   // Stop the servo
				} else if (userInput.equalsIgnoreCase("TH")) {
					System.out.println(String.format("Theoretical values at %d Hz: Min: %04d, Center: %04d, Max: %04d", freq, PCA9685.getServoMinValue(freq), PCA9685.getServoCenterValue(freq), PCA9685.getServoMaxValue(freq)));
				} else if (userInput.toUpperCase().startsWith("PULSE ")) {
					int pulse = Integer.parseInt(userInput.substring("PULSE ".length()));
					System.out.println(String.format("At %d Hz, Value %04d, pulse %.03f", freq, pulse, PCA9685.getPulseFromValue(freq, pulse)));
				} else {
					matcher = patternOne.matcher(userInput);
					if (matcher.matches()) {
						int from = Integer.parseInt(userInput.substring(userInput.indexOf('[') + 1, userInput.indexOf(':')));
						int to = Integer.parseInt(userInput.substring(userInput.indexOf(':') + 1, userInput.indexOf(']')));
						System.out.println(String.format("Range detected in %s", userInput));
						int incr = 1;
						if (from > to) {
							incr = -1;
						}
						for (int value=from; value != to; value+=incr) {
							System.out.println(String.format("Value %04d, pulse %.03f", value, PCA9685.getPulseFromValue(freq, value)));
							servoBoard.setPWM(servoChannel, 0, value);
							TimeUtil.delay(250L);
						}
					} else {
						matcher = patternTwo.matcher(userInput);
						if (matcher.matches()) {
							int value = Integer.parseInt(userInput.substring(userInput.indexOf('[') + 1, userInput.indexOf('-')));
							float duration = Float.parseFloat(userInput.substring(userInput.indexOf('-') + 1, userInput.indexOf(']')));
							System.out.println(String.format("Duration detected in %s", userInput));
							System.out.println(String.format("Value %04d, pulse %.03f", value, PCA9685.getPulseFromValue(freq, value)));
							servoBoard.setPWM(servoChannel, 0, value);
							TimeUtil.delay(Math.round(duration * 1_000L));
							servoBoard.setPWM(servoChannel, 0, 0);   // Stop the servo
						} else {
							try {
								int pwmValue = Integer.parseInt(userInput);
								System.out.println(String.format("From value: %04d, pulse is %.03f", pwmValue, PCA9685.getPulseFromValue(freq, pwmValue)));
								servoBoard.setPWM(servoChannel, 0, pwmValue); // Do it!
							} catch (NumberFormatException nfe) {
								nfe.printStackTrace();
							}
						}
					}
				}
			}
			servoBoard.setPWM(servoChannel, 0, 0);   // Stop the servo
		} else {
			System.out.println("Go on a Pi...");
		}
		System.out.println("Done, bye.");
	}
}
