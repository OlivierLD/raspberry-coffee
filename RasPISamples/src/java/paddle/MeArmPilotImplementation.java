package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import i2c.samples.mearm.MeArmPilot;
import java.io.IOException;
import java.util.Arrays;

/**
 * Commands:
 * - Move forward
 * - Move backward
 * - Turn right
 * - Turn left
 * - Up
 * - Down
 * - Open claw
 * - Close claw
 *
 * This class is an abstraction, that could be used to drive the MeArm,
 * whatever the instructions are received from (joystick, WebSockets, push-buttons, etc).
 *
 */
public class MeArmPilotImplementation {

	private final static GpioController gpio = GpioFactory.getInstance();

	/*
	 * Left & right
	 * BOTTOM: 510  410  310
	 *         |    |    |
	 *         |    |    Right
	 *         |    Center
	 *         Left
	 *
	 * Open & close
	 * CLAW: 400  130
	 *       |    |
	 *       |    Open
	 *       Closed
	 *
	 * Up & Down
	 * LEFT: 230  350
	 *       |    |
	 *       |    High
	 *       Low
	 *
	 * Forward and Backward
	 * RIGHT: 550  430
	 *        |    |
	 *        |    Backward
	 *        Forward
	 *
	 */

	enum ServoRange {
		//     Name      from to   middle
		BOTTOM("BOTTOM", 310, 510, 410), // Left, Right, Middle
		CLAW  ("CLAW",   130, 400),      // Open, Closed
		LEFT  ("LEFT",   230, 350, 290), // Low, High, Middle
		RIGHT ("RIGHT",  430, 550, 490); // Backward, Forward, Middle

		private final String servoName;
		private final int from;
		private final int to;
		private final int middle;

		ServoRange(String servoName, int from, int to) {
			this(servoName, from, to, -1);
		}
		ServoRange(String servoName, int from, int to, int middle) {
			this.servoName = servoName;
			this.from = from;
			this.to = to;
			this.middle = middle;
		}
		public String servoName() { return this.servoName; }
		public int from() { return this.from; }
		public int to() { return this.to; }
		public int middle() { return this.middle; }
	}

	// Original settings.
	private static int currentClawPos         = ServoRange.CLAW.to();       // Closed
	private static int currentLeftRightPos    = ServoRange.BOTTOM.middle(); // Center
	private static int currentUpDownPos       = ServoRange.LEFT.middle();   // Center
	private static int currentBackAndForthPos = ServoRange.RIGHT.middle();  // Middle

	private String status() {
		String status = "";
		status = String.format("Claw: %d, Back n' Forth: %d, Up-Down: %d, Left Right: %d", currentClawPos, currentBackAndForthPos, currentUpDownPos, currentLeftRightPos);
		return status;
	}

	private static void executeCommandList(String[] commands) {
		Arrays.stream(commands).forEach(cmd -> {
			i2c.samples.mearm.MeArmPilot.validateCommand(cmd, -1);
			if ("true".equals(System.getProperty("mearm.pilot", "false"))) {
				i2c.samples.mearm.MeArmPilot.executeCommand(cmd, -1);
			} else {
				System.out.println(cmd);
			}
		});
	}

	public static void init() throws I2CFactory.UnsupportedBusNumberException,
					IOException {

		MeArmPilot.initContext();

		String[] initCommands = {
			"SET_PWM:LEFT,   0, 0",
			"SET_PWM:RIGHT,  0, 0",
			"SET_PWM:CLAW,   0, 0",
			"SET_PWM:BOTTOM, 0, 0",
			"WAIT:1000",
//		"# Center the arm",
			"SET_PWM:BOTTOM, 0, 410",
			"SET_PWM:BOTTOM, 0, 0",
			"WAIT:250",
//		"# Stand up",
			"SET_PWM:RIGHT, 0, 290",
			"SET_PWM:RIGHT, 0, 0",
			"WAIT:250",
//		"# Middle",
			"SET_PWM:LEFT, 0, 490",
			"SET_PWM:LEFT, 0, 0",
			"WAIT:250"
		};
		System.out.println("Initializing servos.");
		executeCommandList(initCommands);
	}

	public static void shutdown() {
		String[] stopCommands = {
						"SET_PWM:LEFT,   0, 0",
						"SET_PWM:RIGHT,  0, 0",
						"SET_PWM:CLAW,   0, 0",
						"SET_PWM:BOTTOM, 0, 0"
		};
		System.out.println("\nStopping servos.");
		executeCommandList(stopCommands);
		gpio.shutdown();
	}

	public int moveForward() {
		return moveForward(10, 1);
	}
	public int moveForward(int inc, int steps) {
		if ("true".equals(System.getProperty("mearm.verbose", "false"))) {
			System.out.println(status());
		}
		int backForthPos = currentBackAndForthPos + inc;
		if (backForthPos > ServoRange.RIGHT.to()) {
			return -1;
		}
		String[] moveForward = {
						"PRINT: \"Moving forward\"",
						String.format("MOVE: %s, %d, %d, %d, 25", ServoRange.RIGHT.servoName(), currentBackAndForthPos, backForthPos, steps)
		};
		executeCommandList(moveForward);
		currentBackAndForthPos = backForthPos;
		return 0;
	}
	public int moveBackward() {
		return moveBackward(10, 1);
	}
	public int moveBackward(int inc, int steps) {
		if ("true".equals(System.getProperty("mearm.verbose", "false"))) {
			System.out.println(status());
		}
		int backForthPos = currentBackAndForthPos - inc;
		if (backForthPos < ServoRange.RIGHT.from()) {
			return -1;
		}
		String[] moveBackward = {
						"PRINT: \"Moving backward\"",
						String.format("MOVE: %s, %d, %d, %d, 25", ServoRange.RIGHT.servoName(), currentBackAndForthPos, backForthPos, steps)
		};
		executeCommandList(moveBackward);
		currentBackAndForthPos = backForthPos;
		return 0;
	}
	public int moveUp() {
		return moveUp(10, 1);
	}
	public int moveUp(int inc, int steps) {
		if ("true".equals(System.getProperty("mearm.verbose", "false"))) {
			System.out.println(status());
		}
		int upDownPos = currentUpDownPos + inc;
		if (upDownPos > ServoRange.LEFT.to()) {
			return -1;
		}
		String[] moveUp = {
						"PRINT: \"Moving up\"",
						String.format("MOVE: %s, %d, %d, %d, 25", ServoRange.LEFT.servoName(), currentUpDownPos, upDownPos, steps)
		};
		executeCommandList(moveUp);
		currentUpDownPos = upDownPos;
		return 0;
	}
	public int moveDown() {
		return moveDown(10, 1);
	}
	public int moveDown(int inc, int steps) {
		if ("true".equals(System.getProperty("mearm.verbose", "false"))) {
			System.out.println(status());
		}
		int upDownPos = currentUpDownPos - inc;
		if (upDownPos < ServoRange.LEFT.from()) {
			return -1;
		}
		String[] moveDown = {
						"PRINT: \"Moving down\"",
						String.format("MOVE: %s, %d, %d, %d, 25", ServoRange.LEFT.servoName(), currentUpDownPos, upDownPos, steps)
		};
		executeCommandList(moveDown);
		currentUpDownPos = upDownPos;
		return 0;
	}
	public int turnRight() {
		return turnRight(10, 1);
	}
	public int turnRight(int inc, int steps) {
		if ("true".equals(System.getProperty("mearm.verbose", "false"))) {
			System.out.println(status());
		}
		int leftRightPos = currentLeftRightPos + inc;
		if (leftRightPos > ServoRange.BOTTOM.to()) {
			return -1;
		}
		String[] turnRight = {
						"PRINT: \"Moving right\"",
						String.format("MOVE: %s, %d, %d, %d, 25", ServoRange.BOTTOM.servoName(), currentLeftRightPos, leftRightPos, steps)
		};
		executeCommandList(turnRight);
		currentLeftRightPos = leftRightPos;
		return 0;
	}
	public int turnLeft() {
		return turnLeft(10, 1);
	}
	public int turnLeft(int inc, int steps) {
		if ("true".equals(System.getProperty("mearm.verbose", "false"))) {
			System.out.println(status());
		}
		int leftRightPos = currentLeftRightPos - inc;
		if (leftRightPos < ServoRange.BOTTOM.from()) {
			return -1;
		}
		String[] turnLeft = {
						"PRINT: \"Moving left\"",
						String.format("MOVE: %s, %d, %d, %d, 25", ServoRange.BOTTOM.servoName(), currentLeftRightPos, leftRightPos, steps)
		};
		executeCommandList(turnLeft);
		currentLeftRightPos = leftRightPos;
		return 0;
	}
	public int openClaw() {
		return openClaw(10, 1);
	}
	public int openClaw(int inc, int steps) {
		if ("true".equals(System.getProperty("mearm.verbose", "false"))) {
			System.out.println(status());
		}
		int clawPos = currentClawPos - inc;
		if (clawPos < ServoRange.CLAW.from()) {
			return -1;
		}
		String[] openClaw = {
						"PRINT: \"Opening the claw\"",
						String.format("MOVE: %s, %d, %d, %d, 25", ServoRange.CLAW.servoName(), currentClawPos, clawPos, steps)
		};
		executeCommandList(openClaw);
		currentClawPos = clawPos;
		return 0;
	}
	public int closeClaw() {
		return closeClaw(10, 1);
	}
	public int closeClaw(int inc, int steps) {
		if ("true".equals(System.getProperty("mearm.verbose", "false"))) {
			System.out.println(status());
		}
		int clawPos = currentClawPos + inc;
		if (clawPos > ServoRange.CLAW.to()) {
			return -1;
		}
		String[] closeClaw = {
						"PRINT: \"Closing the claw\"",
						String.format("MOVE: %s, %d, %d, %d, 25", ServoRange.CLAW.servoName(), currentClawPos, clawPos, steps)
		};
		executeCommandList(closeClaw);
		currentClawPos = clawPos;
		return 0;
	}

	/**
	 * For tests.
	 *
	 * @param args unused
	 * @throws IOException
	 * @throws UnsupportedBusNumberException
	 */
	public static void main(String... args) throws IOException, UnsupportedBusNumberException {
		MeArmPilotImplementation meArm = new MeArmPilotImplementation();
		MeArmPilotImplementation.init();

		// Open and close the claw
		boolean ok = true;
		while (ok) {
			while (meArm.openClaw(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		ok = true;
		while (ok) {
			while (meArm.closeClaw(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}

		// Move right and left
		ok = true;
		while (ok) {
			while (meArm.turnRight(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		ok = true;
		while (ok) {
			while (meArm.turnLeft(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		// Back in the middle
		ok = true;
		while (ok && currentLeftRightPos != ServoRange.BOTTOM.middle()) {
			while (meArm.turnRight(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}

		// Move up & down
		ok = true;
		while (ok) {
			while (meArm.moveUp(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		ok = true;
		while (ok) {
			while (meArm.moveDown(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		// Back in the middle
		ok = true;
		while (ok && currentUpDownPos != ServoRange.LEFT.middle()) {
			while (meArm.moveUp(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}

		// Move back & forth
		ok = true;
		while (ok) {
			while (meArm.moveBackward(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		ok = true;
		while (ok) {
			while (meArm.moveForward(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		// Back in the middle
		ok = true;
		while (ok && currentBackAndForthPos != ServoRange.RIGHT.middle()) {
			while (meArm.moveBackward(10, 10) == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}

		MeArmPilotImplementation.shutdown();
		System.out.println("Yo!");
	}
}
