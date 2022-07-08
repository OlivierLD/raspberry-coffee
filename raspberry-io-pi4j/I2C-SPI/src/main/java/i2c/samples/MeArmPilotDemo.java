package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.samples.mearm.MeArmPilot;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Executes a script containing commands to drive a MeArm robotic arm,
 * like script.01.mearm
 *
 * Uses a PCA9685 (I2C) to drive a MeArm
 *
 * This class refers to a script to know what to do.
 * See the {@link #main} method.
 */
public class MeArmPilotDemo {

	private final static String LEFT_PRM   = "-left:";
	private final static String RIGHT_PRM  = "-right:";
	private final static String BOTTOM_PRM = "-bottom:";
	private final static String CLAW_PRM   = "-claw:";

	/**
	 * Execute a MeArm script. Provide the script name in a System variable, like in
	 * <pre>
	 *   -Dscript.name=script.01.mearm
	 * </pre>
	 * See script.01.mearm for an example.
	 *
	 * 	 * <br/>
	 * 	 * Command line parameters:
	 * 	 * <pre>
	 * 	 * $ java i2c.samples.MeArmPilotInteractiveDemo -left:0 -right:4 -bottom:2 -claw:1
	 * 	 * </pre>
	 * 	 * The numbers are the IDs (0..15) of the channels of the PCA9685. The numbers above are the default channels.
	 *
	 * @param args -left:X -right:X -bottom:X -claw:X. See above
	 * @throws I2CFactory.UnsupportedBusNumberException when I2C bus is not found (if you're not on a Raspberry Pi)
	 * @throws IOException                              when the script cannot be read, for example. File not found or so.
	 */
	public static void main(String... args)
					throws I2CFactory.UnsupportedBusNumberException,
					IOException {
		String scriptName = System.getProperty("script.name");
		if (scriptName == null) {
			throw new RuntimeException("Please provide the script name in -Dscript.name");
		}

		int left = MeArmPilot.DEFAULT_LEFT_SERVO_CHANNEL;
		int right = MeArmPilot.DEFAULT_RIGHT_SERVO_CHANNEL;
		int claw = MeArmPilot.DEFAULT_CLAW_SERVO_CHANNEL;
		int bottom = MeArmPilot.DEFAULT_BOTTOM_SERVO_CHANNEL;

		// Managing parameters
		for (String arg : args) {
			if (arg.startsWith(LEFT_PRM)) {
				String val = arg.substring(LEFT_PRM.length());
				try {
					int ch = Integer.parseInt(val);
					if (ch < 0 || ch > 15) {
						throw new IllegalArgumentException(String.format("Invalid left channel value. Must be in [0..15], found %d", ch));
					} else {
						left = ch;
					}
				} catch (NumberFormatException nfe) {
					System.err.println(nfe.getMessage());
				}
			} else if (arg.startsWith(RIGHT_PRM)) {
				String val = arg.substring(RIGHT_PRM.length());
				try {
					int ch = Integer.parseInt(val);
					if (ch < 0 || ch > 15) {
						throw new IllegalArgumentException(String.format("Invalid right channel value. Must be in [0..15], found %d", ch));
					} else {
						right = ch;
					}
				} catch (NumberFormatException nfe) {
					System.err.println(nfe.getMessage());
				}
			} else if (arg.startsWith(CLAW_PRM)) {
				String val = arg.substring(CLAW_PRM.length());
				try {
					int ch = Integer.parseInt(val);
					if (ch < 0 || ch > 15) {
						throw new IllegalArgumentException(String.format("Invalid claw channel value. Must be in [0..15], found %d", ch));
					} else {
						claw = ch;
					}
				} catch (NumberFormatException nfe) {
					System.err.println(nfe.getMessage());
				}
			} else if (arg.startsWith(BOTTOM_PRM)) {
				String val = arg.substring(BOTTOM_PRM.length());
				try {
					int ch = Integer.parseInt(val);
					if (ch < 0 || ch > 15) {
						throw new IllegalArgumentException(String.format("Invalid bottom channel value. Must be in [0..15], found %d", ch));
					} else {
						bottom = ch;
					}
				} catch (NumberFormatException nfe) {
					System.err.println(nfe.getMessage());
				}
			} else {
				System.out.println(String.format("Unknown CLI prm [%s]", arg));
			}
		}

		// Validate the script
		BufferedReader scriptReader = new BufferedReader(new FileReader(scriptName));
		String line = "";
		boolean keepReading = true;
		int nbLine = 0;
		while (keepReading) {
			line = scriptReader.readLine();
			if (line == null) {
				keepReading = false;
			} else {
				nbLine++;
				System.out.println("Read " + line);
				if (!line.startsWith("#")) {
					MeArmPilot.validateCommand(line, nbLine);
				}
			}
		}
		scriptReader.close();
		System.out.println("> Script is valid. <");

		// Execute the script

		MeArmPilot.initContext(left, claw, bottom, right);

		scriptReader = new BufferedReader(new FileReader(scriptName));
		line = "";
		keepReading = true;
		nbLine = 0;
		while (keepReading) {
			line = scriptReader.readLine();
			if (line == null) {
				keepReading = false;
			} else {
				nbLine++;
//      System.out.println("Executing " + line);
				if (!line.startsWith("#")) {
					MeArmPilot.executeCommand(line, nbLine);
				}
			}
		}
		scriptReader.close();
		System.out.println("> Script completed. <");
	}
}
