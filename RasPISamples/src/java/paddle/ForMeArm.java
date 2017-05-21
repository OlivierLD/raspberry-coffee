package paddle;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.exception.UnsupportedPinPullResistanceException;
import com.pi4j.io.i2c.I2CFactory;
import i2c.samples.mearm.MeArmPilot;
import java.io.IOException;
import java.util.Arrays;
import paddle.buttons.PushButtonInstance;
import raspisamples.adc.JoyStick;
import raspisamples.adc.JoyStickClient;
import raspisamples.adc.TwoJoyStick;
import raspisamples.adc.TwoJoyStickClient;

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
			"SET_PWM:RIGHT, 0, 430",
			"SET_PWM:RIGHT, 0, 0",
			"WAIT:250",
//		"# Middle",
			"SET_PWM:LEFT, 0, 230",
			"SET_PWM:LEFT, 0, 0",
			"WAIT:250"
		};
		System.out.println("Initializing servos.");
		Arrays.stream(initCommands).forEach(cmd -> {
			MeArmPilot.validateCommand(cmd, -1);
			MeArmPilot.executeCommand(cmd, -1);
		});

		TwoJoyStickClient jsc = new TwoJoyStickClient() {
			@Override
			public void setUD1(int v) { // 0..100. 50 in the middle
				float angle = (float) (v - 50) * (9f / 5f);
				System.out.println(String.format("V: %d, UD Angle: %f", v, angle));
				//	  ss1.setAngle(angle); // -90..+90
				// Build the command here
				if (v > 50) { // Forward
					String[] forward = {
						"PRINT: \"Reaching ahead\"",
						"MOVE: RIGHT, 430, 550, 10, 25"
					};
					Arrays.stream(forward).forEach(cmd -> {
						MeArmPilot.validateCommand(cmd, -1);
						MeArmPilot.executeCommand(cmd, -1);
					});
				} else if (v < 50) { // Backward
					String[] backward = {
									"PRINT: \"Backwards\"",
									"MOVE: RIGHT, 550, 430, 10, 25"
					};
					Arrays.stream(backward).forEach(cmd -> {
						MeArmPilot.validateCommand(cmd, -1);
						MeArmPilot.executeCommand(cmd, -1);
					});
				}
			}

			@Override
			public void setLR1(int v) { // 0..100
				float angle = (float) (v - 50) * (9f / 5f);
				System.out.println(String.format("V: %d, LR Angle: %f", v, angle));
//		  ss2.setAngle(angle); // -90..+90
				if (v > 50) { // Right
					String[] turnRight = {
									"PRINT: \"Turning Right\"",
									"MOVE: BOTTOM, 510, 310, 10, 25"
					};
					Arrays.stream(turnRight).forEach(cmd -> {
						MeArmPilot.validateCommand(cmd, -1);
						MeArmPilot.executeCommand(cmd, -1);
					});
				} else if (v < 50) { // Left
					String[] turnLeft = {
									"PRINT: \"Turning Left\"",
									"MOVE: BOTTOM, 410, 510, 10, 25"
					};
					Arrays.stream(turnLeft).forEach(cmd -> {
						MeArmPilot.validateCommand(cmd, -1);
						MeArmPilot.executeCommand(cmd, -1);
					});
				}
			}

			@Override
			public void setUD2(int v) { // 0..100. 50 in the middle
				float angle = (float) (v - 50) * (9f / 5f);
				System.out.println(String.format("V: %d, UD Angle: %f", v, angle));
				//	  ss1.setAngle(angle); // -90..+90
				// Build the command here
				if (v > 50) { // Higher
					String[] higher = {
									"PRINT: \"Higher\"",
									"MOVE: LEFT, 230, 350, 10, 25"
					};
					Arrays.stream(higher).forEach(cmd -> {
						MeArmPilot.validateCommand(cmd, -1);
						MeArmPilot.executeCommand(cmd, -1);
					});
				} else if (v < 50) { // Lower
					String[] lower = {
									"PRINT: \"Lower\"",
									"MOVE: LEFT, 350, 230, 10, 25"
					};
					Arrays.stream(lower).forEach(cmd -> {
						MeArmPilot.validateCommand(cmd, -1);
						MeArmPilot.executeCommand(cmd, -1);
					});
				}
			}

			@Override
			public void setLR2(int v) { // 0..100
				float angle = (float) (v - 50) * (9f / 5f);
				System.out.println(String.format("V: %d, LR Angle: %f", v, angle));
//		  ss2.setAngle(angle); // -90..+90
				if (v > 50) { // Open
					String[] openClaw = {
									"PRINT: \"Opening the claw\"",
									"MOVE: CLAW, 400, 130, 10, 25"
					};
					Arrays.stream(openClaw).forEach(cmd -> {
						MeArmPilot.validateCommand(cmd, -1);
						MeArmPilot.executeCommand(cmd, -1);
					});
				} else if (v < 50) { // Close TODO
					String[] closeClaw = {
									"PRINT: \"Closing the claw\"",
									"MOVE: CLAW, 130, 400, 10, 25"
					};
					Arrays.stream(closeClaw).forEach(cmd -> {
						MeArmPilot.validateCommand(cmd, -1);
						MeArmPilot.executeCommand(cmd, -1);
					});
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
//		ss1.stop();
//		ss2.stop();
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
				Arrays.stream(stopCommands).forEach(cmd -> {
					MeArmPilot.validateCommand(cmd, -1);
					MeArmPilot.executeCommand(cmd, -1);
				});

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
		System.out.println("\nDone reading buttons.");
		gpio.shutdown();
	}
}
