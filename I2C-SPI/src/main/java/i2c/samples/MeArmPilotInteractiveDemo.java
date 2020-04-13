package i2c.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.samples.mearm.MeArmPilot;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static utils.StaticUtil.userInput;

/**
 * Drive a MeArm robotic arm, interactively, from the CLI
 *
 * Uses a PCA9685 (I2C) to drive the MeArm's 4 servos.
 * Relies on {@link MeArmPilot}
 *
 * See the {@link #main} method.
 */
public class MeArmPilotInteractiveDemo {

	private final static String LEFT_PRM   = "-left:";
	private final static String RIGHT_PRM  = "-right:";
	private final static String BOTTOM_PRM = "-bottom:";
	private final static String CLAW_PRM   = "-claw:";

	/**
	 * Execute MeArm pilot commands provided by the user, from the CLI.
	 * <br/>
	 * Command line parameters:
	 * <pre>
	 * $ java i2c.samples.MeArmPilotInteractiveDemo -left:0 -right:4 -bottom:2 -claw:1
	 * </pre>
	 * The numbers are the IDs (0..15) of the channels of the PCA9685. The numbers above are the default channels.
	 *
	 * @param args -left:X -right:X -bottom:X -claw:X. See above
	 * @throws I2CFactory.UnsupportedBusNumberException when I2C bus is not found (if you're not on a Raspberry Pi)
	 */
	public static void main(String... args)
					throws I2CFactory.UnsupportedBusNumberException {
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

		try {
			MeArmPilot.initContext(left, claw, bottom, right);
		} catch (I2CFactory.UnsupportedBusNumberException oops) {
			System.out.println(">> Ooops!, wrong bus... Moving on anyway, but without the board.");
		}

		// Initializing MeArm pos
		MeArmPilot.runMacro(MeArmPilot.initStop());
		MeArmPilot.runMacro(MeArmPilot.initialPosition());

		// Now looping on user input.
		boolean keepAsking = true;
		int nbCommand = 0;
		System.out.println("Enter 'Q' at the prompt to quit.");
		System.out.println("Type HELP for help.");
		while (keepAsking) {
			String cmd = userInput(String.format("%d> ", ++nbCommand));
			if ("Q".equalsIgnoreCase(cmd)) {
				keepAsking = false;
			} else if (!cmd.trim().isEmpty()) {
				boolean ok = true;
				try {
					MeArmPilot.validateCommand(cmd, nbCommand);
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
					ok = false;
				}
				if (ok) {
					MeArmPilot.executeCommand(cmd, nbCommand);
				}
			}
	  }
	  // Done, quitting.
		System.out.println("Parking servos");
		MeArmPilot.runMacro(MeArmPilot.initialPosition());
		MeArmPilot.runMacro("WAIT:1000");
		MeArmPilot.runMacro(MeArmPilot.closeClaw());
		MeArmPilot.runMacro("WAIT:500");
		MeArmPilot.runMacro(MeArmPilot.initStop());
		System.out.println("Bye.");
	}
}
