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
	/**
	 * Execute a MeArm script. Provide the script name in a System variable, like in
	 * <pre>
	 *   -Dscript.name=script.01.mearm
	 * </pre>
	 * See script.01.mearm for an example.
	 *
	 * @param args None required.
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

		// 1 - Validate the script
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

		// 2 - Execute the script

		MeArmPilot.initContext();

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
