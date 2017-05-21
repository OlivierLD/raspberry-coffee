package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import java.io.IOException;
import java.util.Arrays;
import raspisamples.adc.TwoJoyStick;
import raspisamples.adc.TwoJoyStickClient;

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
 */
public class MeArmPilot {

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
		BOTTOM("BOTTOM", 310, 510, 410), // Left, Right, Middle
		CLAW("CLAW", 130, 400),          // Open, Closed
		LEFT("LEFT", 230, 350, 290),     // Low, High, Middle
		RIGHT("RIGHT", 430, 550, 490);   // Backward, Forward, Middle

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

	private static int currentClawPos         = ServoRange.CLAW.to(); // Closed
	private static int currentLeftRightPos    = ServoRange.BOTTOM.middle(); // Center
	private static int currentUpDownPos       = ServoRange.LEFT.middle(); // Center
	private static int currentBackAndForthPos = ServoRange.RIGHT.middle(); // Middle

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

		i2c.samples.mearm.MeArmPilot.initContext();

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
	public void moveForward() {

	}
	public void moveBackward() {

	}
	public void moveUp() {

	}
	public void moveDown() {

	}
	public void turnRight() {

	}
	public void turnLeft() {

	}
	public int openClaw() {
		// Close 400, Open 130
		int clawPos = currentClawPos - 1;
		if (clawPos < ServoRange.CLAW.from()) {
			return -1;
		}

		String[] openClaw = {
						"PRINT: \"Opening the claw\"",
						String.format("MOVE: CLAW, %d, %d, 10, 25", currentClawPos, clawPos)
		};
		executeCommandList(openClaw);
		currentClawPos = clawPos;
		return 0;
	}
	public int closeClaw() {
		// Close 400, Open 130
		int clawPos = currentClawPos + 1;
		if (clawPos > ServoRange.CLAW.to()) {
			return -1;
		}

		String[] closeClaw = {
						"PRINT: \"Closing the claw\"",
						String.format("MOVE: CLAW, %d, %d, 10, 25", currentClawPos, clawPos)
		};
		executeCommandList(closeClaw);
		currentClawPos = clawPos;
		return 0;
	}

	public static void main(String... args) throws IOException, UnsupportedBusNumberException {
		MeArmPilot meArm = new MeArmPilot();
		MeArmPilot.init();

		// Open the claw
		boolean ok = true;
		while (ok) {
			while (meArm.openClaw() == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		ok = true;
		while (ok) {
			while (meArm.closeClaw() == 0) {
				try { Thread.sleep(10); } catch (Exception whatever) {}
			}
			ok = false;
		}
		MeArmPilot.shutdown();
		System.out.println("Yo!");
	}
}
