package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CFactory;
import i2c.samples.mearm.MeArmPilot;
import java.io.IOException;
import java.util.Arrays;
import joystick.adc.TwoJoyStick;
import joystick.adc.TwoJoyStickClient;

/**
 * Two joysticks.
 *
 * Joystick 1:
 * - Move forward
 * - Move backward
 * - Turn right
 * - Turn left
 *
 * Joystick 2:
 * - Up
 * - Down
 * - Open claw
 * - Close claw
 */
public class ForMeArm {

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
			MeArmPilot.validateCommand(cmd, -1);
			if ("true".equals(System.getProperty("mearm.pilot", "false"))) {
				MeArmPilot.executeCommand(cmd, -1);
			} else {
				System.out.println(cmd);
			}
		});
	}

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException,
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

		TwoJoyStickClient jsc = new TwoJoyStickClient() {
			@Override
			public void setUD1(int v) { // 0..100. 50 in the middle
				System.out.println(String.format("UD1 V: %d", v));
				// Build the command here
				if (v >= 50) { // Forward
					int goForwardAmplitude = ServoRange.RIGHT.to() - ServoRange.RIGHT.middle();
					int servoRange = v - 50;
					int forwardPos = ServoRange.RIGHT.to() - (int)Math.round(((float)servoRange / 50f) * (float)goForwardAmplitude);
//				System.out.println(String.format(">> Calculated: V:%d, Pos:%d", v, clawPos));
					String[] forward = {
									"PRINT: \"Forward\"",
									String.format("MOVE: RIGHT, %d, %d, 10, 25", currentBackAndForthPos, forwardPos)
					};
					executeCommandList(forward);
					currentBackAndForthPos = forwardPos;
				} else if (v < 50) { // Backward

					int goBackwardAmplitude = ServoRange.RIGHT.middle() - ServoRange.RIGHT.from();
					int servoRange = 50 - v;
					int backwardPos = ServoRange.RIGHT.middle() - (int)Math.round(((float)servoRange / 50f) * (float)goBackwardAmplitude);
//				System.out.println(String.format(">> Calculated Backward: V:%d, Pos:%d", v, backwardPos));
					String[] lower = {
									"PRINT: \"Backward\"",
									String.format("MOVE: RIGHT, %d, %d, 10, 25", currentBackAndForthPos, backwardPos)
					};
					executeCommandList(lower);
					currentBackAndForthPos = backwardPos;
				}
			}

			@Override
			public void setLR1(int v) { // 0..100
				System.out.println(String.format("LR1 V: %d", v));
				if (v >= 50) { // Right
					int goRightAmplitude = ServoRange.BOTTOM.to() - ServoRange.BOTTOM.middle();
					int servoRange = v - 50;
					int rightPos = ServoRange.BOTTOM.to() - (int)Math.round(((float)servoRange / 50f) * (float)goRightAmplitude);
//				System.out.println(String.format(">> Calculated: V:%d, Pos:%d", v, clawPos));
					String[] turnRight = {
									"PRINT: \"Turning Right\"",
									String.format("MOVE: BOTTOM, %d, %d, 10, 25", currentLeftRightPos, rightPos)
					};
					executeCommandList(turnRight);
					currentLeftRightPos = rightPos;
				} else if (v < 50) { // Left

					int goLeftAmplitude = ServoRange.BOTTOM.middle() - ServoRange.BOTTOM.from();
					int servoRange = v - 50;
					int leftPos = ServoRange.BOTTOM.middle() - (int)Math.round(((float)servoRange / 50f) * (float)goLeftAmplitude);
//				System.out.println(String.format(">> Calculated: V:%d, Pos:%d", v, clawPos));
					String[] turnLeft = {
									"PRINT: \"Turning Left\"",
									String.format("MOVE: BOTTOM, %d, %d, 10, 25", currentLeftRightPos, leftPos)
					};
					executeCommandList(turnLeft);
					currentLeftRightPos = leftPos;
				}
			}

			@Override
			public void setUD2(int v) { // 0..100. 50 in the middle
				System.out.println(String.format("UD2 V: %d", v));
				// Build the command here
				if (v >= 50) { // Higher
					int goHighAmplitude = ServoRange.LEFT.to() - ServoRange.LEFT.middle();
					int servoRange = v - 50;
					int highPos = ServoRange.LEFT.to() - (int)Math.round(((float)servoRange / 50f) * (float)goHighAmplitude);
//				System.out.println(String.format(">> Calculated: V:%d, Pos:%d", v, clawPos));
					String[] higher = {
									"PRINT: \"Higher\"",
									String.format("MOVE: LEFT, %d, %d, 10, 25", currentUpDownPos, highPos)
					};
					executeCommandList(higher);
					currentUpDownPos = highPos;
				} else if (v < 50) { // Lower
					int goLowAmplitude = ServoRange.LEFT.middle() - ServoRange.LEFT.from();
					int servoRange = 50 - v;
					int lowPos = ServoRange.LEFT.middle() - (int)Math.round(((float)servoRange / 50f) * (float)goLowAmplitude);
//				System.out.println(String.format(">> Calculated: V:%d, Pos:%d", v, clawPos));
					String[] lower = {
									"PRINT: \"Lower\"",
									String.format("MOVE: LEFT, %d, %d, 10, 25", currentUpDownPos, lowPos)
					};
					executeCommandList(lower);
					currentUpDownPos = lowPos;
				}
			}

			@Override
			public void setLR2(int v) { // 0..100
				System.out.println(String.format("LR2 V: %d", v));
				if (v >= 50) { // Open
					// Close 400, Open 130
					int clawAmplitude = ServoRange.CLAW.to() - ServoRange.CLAW.from();
					int servoRange = v - 50;
					int clawPos = ServoRange.CLAW.to() - (int)Math.round(((float)servoRange / 50f) * (float)clawAmplitude);
//				System.out.println(String.format(">> Calculated: V:%d, Pos:%d", v, clawPos));
					String[] openClaw = {
									"PRINT: \"Moving the claw\"",
									String.format("MOVE: CLAW, %d, %d, 10, 25", currentClawPos, clawPos)
					};
					executeCommandList(openClaw);
					currentClawPos = clawPos;
				} else if (v < 50) { // Close
					System.out.println("Not used.");
//					String[] closeClaw = {
//									"PRINT: \"Closing the claw\"",
//									"MOVE: CLAW, 130, 400, 10, 25"
//					};
//					Arrays.stream(closeClaw).forEach(cmd -> {
//						MeArmPilot.validateCommand(cmd, -1);
//						MeArmPilot.executeCommand(cmd, -1);
//					});
				}
			}
		};

		/*
		 * MCP3008 uses (by default) pins:
		 * #1  - 3.3V
		 * #6  - GND
		 * #12 - GPIO_18, RaspiPin.GPIO_01
		 * #16 - GPIO_23, RaspiPin.GPIO_04
		 * #18 - GPIO_24, RaspiPin.GPIO_05
		 * #22 - GPIO_25, RaspiPin.GPIO_06
		 *
		 */
		Thread joystickThread = new Thread(() -> {
			try {
				new TwoJoyStick(jsc, false);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				System.out.println("\nBye JoySticks");
			}
		});
		joystickThread.start();
		System.out.println("Joystick thread started");

		// Now wait for Ctrl+C
		Thread waiter = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			synchronized (waiter) {
				// Stop servos
				String[] stopCommands = {
					"SET_PWM:LEFT,   0, 0",
					"SET_PWM:RIGHT,  0, 0",
					"SET_PWM:CLAW,   0, 0",
					"SET_PWM:BOTTOM, 0, 0"
				};
				System.out.println("\nStopping servos.");
				executeCommandList(stopCommands);

				waiter.notify();
				try {
					Thread.sleep(20);
				} catch (Exception ex) {
				}
				System.out.println("Bye main thread");
			}
		}));
		System.out.println("Ready...");
		synchronized (waiter) {
			try {
				waiter.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("\nDone reading joysticks.");
		gpio.shutdown();
	}
}
