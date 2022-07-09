package feedback.v1;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CFactory;
import i2c.servo.PCA9685;
import utils.PinUtil;
import utils.StringUtils;
import utils.SystemUtils;
import utils.gpio.StringToGPIOPin;

import static utils.StringUtils.lpad;

/**
 * To measure a voltage, between 0 and 3.3V or 5.0V (See -DvRef= )
 *
 * This program parameters:
 *
 * --servo-channel:0
 *
 * --knob-channel:0
 * --feedback-channel:1
 *
 * --servo-freq:XX
 * --servo-stop-pwm:XX
 * --servo-forward-pwm:XX
 * --servo-backward-pwm:XX
 */
public class FeedbackPotsServo {

	private final static boolean DEBUG = "true".equals(System.getProperty("debug", "false"));

	private static double vRef = 3.3;
	static {
		vRef = Double.parseDouble(System.getProperty("vRef", String.valueOf(vRef)));
	}

	private static boolean go = true;
	private static int feedbackChannel =
			MCPReader.MCP3008InputChannels.CH1.ch(); // Between 0 and 7, 8 channels on the MCP3008
	private static int knobChannel =
			MCPReader.MCP3008InputChannels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

	private static int servoChannel     =   0;
	private static int servoFreq        =  60;
	private static int servoStopPWM     = 300;
	private static int servoForwardPWM  = 400;
	private static int servoBackwardPWM = 200;

	private static int minDiff = 5;


	private static final String MISO_PRM_PREFIX = "--miso:";
	private static final String MOSI_PRM_PREFIX = "--mosi:";
	private static final String CLK_PRM_PREFIX  =  "--clk:";
	private static final String CS_PRM_PREFIX   =   "--cs:";

	private static final String MIN_DIFF_PREFIX           = "--min-diff:";

	private static final String KNOB_CHANNEL_PREFIX       = "--knob-channel:";
	private static final String FEEDBACK_CHANNEL_PREFIX   = "--feedback-channel:";

	private static final String SERVO_CHANNEL_PREFIX      = "--servo-channel:";

	private static final String SERVO_FREQ_PREFIX         = "--servo-freq:";
	private static final String SERVO_STOP_PWM_PREFIX     = "--servo-stop-pwm:";
	private static final String SERVO_FORWARD_PWM_PREFIX  = "--servo-forward-pwm:";
	private static final String SERVO_BACKWARD_PWM_PREFIX = "--servo-backward-pwm:";

	public static void main(String... args) throws Exception {

		// Default pins
		Pin miso = StringToGPIOPin.stringToGPIOPin(PinUtil.GPIOPin.GPIO_13.pin());
		Pin mosi = StringToGPIOPin.stringToGPIOPin(PinUtil.GPIOPin.GPIO_12.pin());
		Pin clk  = StringToGPIOPin.stringToGPIOPin(PinUtil.GPIOPin.GPIO_14.pin());
		Pin cs   = StringToGPIOPin.stringToGPIOPin(PinUtil.GPIOPin.GPIO_10.pin());

		System.out.println(String.format("Usage is java %s %s%d %s%d %s%d %s%d \\\n\t%s%d %s%d %s%d %s%d %s%d %s%d %s%d ",
				FeedbackPotsServo.class.getName(),       // <- WhoooAhhhaahahha!
				MISO_PRM_PREFIX,  PinUtil.findByPin(miso.getName()).gpio(),
				MOSI_PRM_PREFIX,  PinUtil.findByPin(mosi.getName()).gpio(),
				CLK_PRM_PREFIX,   PinUtil.findByPin(clk.getName()).gpio(),
				CS_PRM_PREFIX,    PinUtil.findByPin(cs.getName()).gpio(),
				KNOB_CHANNEL_PREFIX, knobChannel, //
				FEEDBACK_CHANNEL_PREFIX, feedbackChannel,
				SERVO_CHANNEL_PREFIX, servoChannel,
				SERVO_FREQ_PREFIX, servoFreq,
				SERVO_STOP_PWM_PREFIX, servoStopPWM,
				SERVO_FORWARD_PWM_PREFIX, servoForwardPWM,
				SERVO_BACKWARD_PWM_PREFIX, servoBackwardPWM,
				MIN_DIFF_PREFIX, minDiff));
		System.out.println("Values above are default values (GPIO/BCM numbers).");
		System.out.println();

		if (args.length > 0) {
			String pinValue = "";
			int pin;
			for (String prm : args) {
				if (prm.startsWith(MISO_PRM_PREFIX)) {
					pinValue = prm.substring(MISO_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						miso = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(MOSI_PRM_PREFIX)) {
					pinValue = prm.substring(MOSI_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						mosi = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CLK_PRM_PREFIX)) {
					pinValue = prm.substring(CLK_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						clk = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CS_PRM_PREFIX)) {
					pinValue = prm.substring(CS_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						cs = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(KNOB_CHANNEL_PREFIX)) {
					String chValue = prm.substring(KNOB_CHANNEL_PREFIX.length());
					try {
						knobChannel = Integer.parseInt(chValue);
						if (knobChannel > 7 || knobChannel < 0) {
							throw new RuntimeException("Knob Channel in [0..7] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(FEEDBACK_CHANNEL_PREFIX)) {
					String chValue = prm.substring(FEEDBACK_CHANNEL_PREFIX.length());
					try {
						feedbackChannel = Integer.parseInt(chValue);
						if (feedbackChannel > 7 || feedbackChannel < 0) {
							throw new RuntimeException("Feedback Channel in [0..7] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(SERVO_CHANNEL_PREFIX)) {
					String chValue = prm.substring(SERVO_CHANNEL_PREFIX.length());
					try {
						servoChannel = Integer.parseInt(chValue);
						if (servoChannel > 15 || servoChannel < 0) {
							throw new RuntimeException("Servo Channel in [0..15] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(SERVO_FREQ_PREFIX)) {
					String strValue = prm.substring(SERVO_FREQ_PREFIX.length());
					try {
						servoFreq = Integer.parseInt(strValue);
						if (servoFreq > 1_000 || servoFreq < 40) {
							throw new RuntimeException("Servo Freq in [40..1000] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(SERVO_STOP_PWM_PREFIX)) {
					String strValue = prm.substring(SERVO_STOP_PWM_PREFIX.length());
					try {
						servoStopPWM = Integer.parseInt(strValue);
						if (servoStopPWM > 4_095 || servoStopPWM < 0) {
							throw new RuntimeException("Servo PWM in [0..4095] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(SERVO_FORWARD_PWM_PREFIX)) {
					String strValue = prm.substring(SERVO_FORWARD_PWM_PREFIX.length());
					try {
						servoForwardPWM = Integer.parseInt(strValue);
						if (servoForwardPWM > 4_095 || servoForwardPWM < 0) {
							throw new RuntimeException("Servo PWM in [0..4095] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(SERVO_BACKWARD_PWM_PREFIX)) {
					String strValue = prm.substring(SERVO_BACKWARD_PWM_PREFIX.length());
					try {
						servoBackwardPWM = Integer.parseInt(strValue);
						if (servoBackwardPWM > 4_095 || servoBackwardPWM < 0) {
							throw new RuntimeException("Servo PWM in [0..4095] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(MIN_DIFF_PREFIX)) {
					String strValue = prm.substring(MIN_DIFF_PREFIX.length());
					try {
						minDiff = Integer.parseInt(strValue);
						if (minDiff < 0) {
							throw new RuntimeException("Min Diff > 0 please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else {
					// What?
					System.err.println(String.format("Un-managed prm: %s", prm));
				}
			}
		}

		System.out.println(String.format("Reading MCP3008 on channels %d and %d", knobChannel, feedbackChannel));
		System.out.println(
				" Wiring of the MCP3008-SPI (without power supply):\n" +
						" +---------++-----------------------------------------------+\n" +
						" | MCP3008 || Raspberry Pi                                  |\n" +
						" +---------++------+------------+------+---------+----------+\n" +
						" |         || Pin# | Name       | Role | GPIO    | wiringPI |\n" +
						" |         ||      |            |      | /BCM    | /PI4J    |\n" +
						" +---------++------+------------+------+---------+----------+");
		System.out.println(String.format(" | CLK (13)|| #%02d  | %s | CLK  | GPIO_%02d | %02d       |",
				PinUtil.findByPin(clk.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(clk.getName()).pinName(), 10, " "),
				PinUtil.findByPin(clk.getName()).gpio(),
				PinUtil.findByPin(clk.getName()).wiringPi()));
		System.out.println(String.format(" | Din (11)|| #%02d  | %s | MOSI | GPIO_%02d | %02d       |",
				PinUtil.findByPin(mosi.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(mosi.getName()).pinName(), 10, " "),
				PinUtil.findByPin(mosi.getName()).gpio(),
				PinUtil.findByPin(mosi.getName()).wiringPi()));
		System.out.println(String.format(" | Dout(12)|| #%02d  | %s | MISO | GPIO_%02d | %02d       |",
				PinUtil.findByPin(miso.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(miso.getName()).pinName(), 10, " "),
				PinUtil.findByPin(miso.getName()).gpio(),
				PinUtil.findByPin(miso.getName()).wiringPi()));
		System.out.println(String.format(" | CS  (10)|| #%02d  | %s | CS   | GPIO_%02d | %02d       |",
				PinUtil.findByPin(cs.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(cs.getName()).pinName(), 10, " "),
				PinUtil.findByPin(cs.getName()).gpio(),
				PinUtil.findByPin(cs.getName()).wiringPi()));
		System.out.println(" +---------++------+------------+-----+----------+----------+");
		System.out.println("Raspberry Pi is the Master, MCP3008 is the Slave:");
		System.out.println("- Dout on the MCP3008 goes to MISO on the RPi");
		System.out.println("- Din on the MCP3008 goes to MOSI on the RPi");
		System.out.println("Pins on the MCP3008 are numbered from 1 to 16, beginning top left, counter-clockwise.");
		System.out.println("       +--------+ ");
		System.out.println(String.format("%s CH0 -+  1  16 +- Vdd ",  (knobChannel == 0 || feedbackChannel == 0 ? "*" : " ")));
		System.out.println(String.format("%s CH1 -+  2  15 +- Vref ", (knobChannel == 1 || feedbackChannel == 1 ? "*" : " ")));
		System.out.println(String.format("%s CH2 -+  3  14 +- aGnd ", (knobChannel == 2 || feedbackChannel == 2 ? "*" : " ")));
		System.out.println(String.format("%s CH3 -+  4  13 +- CLK ",  (knobChannel == 3 || feedbackChannel == 3 ? "*" : " ")));
		System.out.println(String.format("%s CH4 -+  5  12 +- Dout ", (knobChannel == 4 || feedbackChannel == 4 ? "*" : " ")));
		System.out.println(String.format("%s CH5 -+  6  11 +- Din ",  (knobChannel == 5 || feedbackChannel == 5 ? "*" : " ")));
		System.out.println(String.format("%s CH6 -+  7  10 +- CS ",   (knobChannel == 6 || feedbackChannel == 6 ? "*" : " ")));
		System.out.println(String.format("%s CH7 -+  8   9 +- dGnd ", (knobChannel == 7 || feedbackChannel == 7 ? "*" : " ")));
		System.out.println("       +--------+ ");

		// Compose mapping for PinUtil
		String[] map = new String[4];
		map[0] = String.valueOf(PinUtil.findByPin(clk.getName()).pinNumber()) + ":" + "CLK";
		map[1] = String.valueOf(PinUtil.findByPin(miso.getName()).pinNumber()) + ":" + "Dout";
		map[2] = String.valueOf(PinUtil.findByPin(mosi.getName()).pinNumber()) + ":" + "Din";
		map[3] = String.valueOf(PinUtil.findByPin(cs.getName()).pinNumber()) + ":" + "CS";

		PinUtil.print(map);

		System.out.println(String.format("Actual values:\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d\n\t%s%d",
				MISO_PRM_PREFIX,  PinUtil.findByPin(miso.getName()).gpio(),
				MOSI_PRM_PREFIX,  PinUtil.findByPin(mosi.getName()).gpio(),
				CLK_PRM_PREFIX,   PinUtil.findByPin(clk.getName()).gpio(),
				CS_PRM_PREFIX,    PinUtil.findByPin(cs.getName()).gpio(),
				KNOB_CHANNEL_PREFIX, knobChannel, //
				FEEDBACK_CHANNEL_PREFIX, feedbackChannel,
				SERVO_CHANNEL_PREFIX, servoChannel,
				SERVO_FREQ_PREFIX, servoFreq,
				SERVO_STOP_PWM_PREFIX, servoStopPWM,
				SERVO_FORWARD_PWM_PREFIX, servoForwardPWM,
				SERVO_BACKWARD_PWM_PREFIX, servoBackwardPWM,
				MIN_DIFF_PREFIX, minDiff));

		MCPReader.initMCP(MCPReader.MCPFlavor.MCP3008, miso, mosi, clk, cs);

		PCA9685 servoBoard = null;

		try {
			servoBoard = new PCA9685();
			servoBoard.setPWMFreq(servoFreq); // Set frequency in Hz
		} catch (I2CFactory.UnsupportedBusNumberException ubne) {
			throw ubne;
		}

		// Display default theoretical values
		System.out.println(String.format("Theoretical values: Min: %04d, Center: %04d, Max: %04d",
				PCA9685.getServoMinValue(servoFreq),
				PCA9685.getServoCenterValue(servoFreq),
				PCA9685.getServoMaxValue(servoFreq)));

		System.out.println("System data:");
		try {
			System.out.println(String.format("\tCore Voltage %s", SystemUtils.getCoreVoltage()));
			System.out.println(String.format("\tCPU Temp %s", SystemUtils.getCPUTemperature()));
			System.out.println(String.format("\tCPU Load %s", SystemUtils.getCPULoad()));
		} catch (Exception ex) {
			throw ex;
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nShutting down.");
			go = false;
			synchronized (Thread.currentThread()) {
				Thread.currentThread().notify();
			}
		}, "Shutdown Hook"));

		// Reading loop
		System.out.println("Starting reading the ADC");
		servoBoard.setPWM(servoChannel, 0, 0);   // Stop the servo
		while (go) {
			int knob = MCPReader.readMCP(knobChannel);
			int feedback = MCPReader.readMCP(feedbackChannel);

			if (DEBUG) {
				System.out.println(String.format("Read ADC: knob=%d, feedback=%d", knob, feedback));
			}
			if (Math.abs(knob - feedback) > minDiff) {  // Now we're talking!
				if (DEBUG) {
					System.out.println(String.format("Difference detected: knob=%d, feedback=%d, moving %s", knob, feedback, (knob > feedback) ? "forward" : "backward"));
				}
				int direction = (knob > feedback) ? servoForwardPWM : servoBackwardPWM;
				while (Math.abs(knob - feedback) > minDiff && go) {

					// Start moving
					if (DEBUG) {
						System.out.println(String.format("Value %04d, pulse %.03f", direction, PCA9685.getPulseFromValue(servoFreq, direction)));
					}
					servoBoard.setPWM(servoChannel, 0, direction);

					knob = MCPReader.readMCP(knobChannel);
					feedback = MCPReader.readMCP(feedbackChannel);
					if (DEBUG) {
						System.out.println(String.format("\tWhile moving, read ADC: knob=%d, feedback=%d", knob, feedback));
					}
					try { // TODO See if the wait is really required...
						synchronized (Thread.currentThread()) {
							Thread.currentThread().wait(50L);
						}
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}

				}
				// Stop moving
				servoBoard.setPWM(servoChannel, 0, 0);   // Stop the servo

				if (DEBUG) {
					System.out.println("Resuming watch");
				}
			}

			try {
				synchronized (Thread.currentThread()) {
					Thread.currentThread().wait(100L);
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		System.out.println("Bye, freeing resources.");
		servoBoard.setPWM(servoChannel, 0, 0);   // Stop the servo
		MCPReader.shutdownMCP();
	}
}
