package feedback.v1;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
import utils.StringUtils;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.Function;

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

	private static final String MISO_PRM_PREFIX = "--miso:";
	private static final String MOSI_PRM_PREFIX = "--mosi:";
	private static final String CLK_PRM_PREFIX  =  "--clk:";
	private static final String CS_PRM_PREFIX   =   "--cs:";

	private static final String KNOB_CHANNEL_PREFIX       = "--knob-channel:";
	private static final String FEEDBACK_CHANNEL_PREFIX   = "--feedback-channel:";

	private static final String SERVO_CHANNEL_PREFIX      = "--servo-channel:";

	private static final String SERVO_FREQ_PREFIX         = "--servo-freq:";
	private static final String SERVO_STOP_PWM_PREFIX     = "--servo-stop-pwm:";
	private static final String SERVO_FORWARD_PWM_PREFIX  = "--servo-forward-pwm:";
	private static final String SERVO_BACKWARD_PWM_PREFIX = "--servo-backward-pwm:";

	private static Function<Integer, Double> adcToDegTransformer = x -> (((x / 1023.0) * 300d) - (300d / 2)); // Default behavior

	public static void main(String... args) {

		// Default pins
		Pin miso = PinUtil.GPIOPin.GPIO_13.pin();
		Pin mosi = PinUtil.GPIOPin.GPIO_12.pin();
		Pin clk  = PinUtil.GPIOPin.GPIO_14.pin();
		Pin cs   = PinUtil.GPIOPin.GPIO_10.pin();

		System.out.println(String.format("Usage is java %s %s%d %s%d %s%d %s%d %s%d",
				FeedbackPotsServo.class.getName(),       // <- WhoooAhhhaahahha!
				MISO_PRM_PREFIX,  PinUtil.findByPin(miso).gpio(),
				MOSI_PRM_PREFIX,  PinUtil.findByPin(mosi).gpio(),
				CLK_PRM_PREFIX,   PinUtil.findByPin(clk).gpio(),
				CS_PRM_PREFIX,    PinUtil.findByPin(cs).gpio(),
				KNOB_CHANNEL_PREFIX, knobChannel));
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
						miso = PinUtil.getPinByGPIONumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(MOSI_PRM_PREFIX)) {
					pinValue = prm.substring(MOSI_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						mosi = PinUtil.getPinByGPIONumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CLK_PRM_PREFIX)) {
					pinValue = prm.substring(CLK_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						clk = PinUtil.getPinByGPIONumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CS_PRM_PREFIX)) {
					pinValue = prm.substring(CS_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						cs = PinUtil.getPinByGPIONumber(pin);
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
						if (servoStopPWM > 1_000 || servoStopPWM < 40) {
							throw new RuntimeException("Servo PWM in [0..4095] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(SERVO_FORWARD_PWM_PREFIX)) {
					String strValue = prm.substring(SERVO_FORWARD_PWM_PREFIX.length());
					try {
						servoForwardPWM = Integer.parseInt(strValue);
						if (servoForwardPWM > 1_000 || servoForwardPWM < 40) {
							throw new RuntimeException("Servo PWM in [0..4095] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(SERVO_BACKWARD_PWM_PREFIX)) {
					String strValue = prm.substring(SERVO_BACKWARD_PWM_PREFIX.length());
					try {
						servoForwardPWM = Integer.parseInt(strValue);
						if (servoBackwardPWM > 1_000 || servoBackwardPWM < 40) {
							throw new RuntimeException("Servo PWM in [0..4095] please");
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

		System.out.println(String.format("Reading MCP3008 on channel %d", knobChannel));
		System.out.println(
				" Wiring of the MCP3008-SPI (without power supply):\n" +
						" +---------++-----------------------------------------------+\n" +
						" | MCP3008 || Raspberry Pi                                  |\n" +
						" +---------++------+------------+------+---------+----------+\n" +
						" |         || Pin# | Name       | Role | GPIO    | wiringPI |\n" +
						" |         ||      |            |      | /BCM    | /PI4J    |\n" +
						" +---------++------+------------+------+---------+----------+");
		System.out.println(String.format(" | CLK (13)|| #%02d  | %s | CLK  | GPIO_%02d | %02d       |",
				PinUtil.findByPin(clk).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(clk).pinName(), 10, " "),
				PinUtil.findByPin(clk).gpio(),
				PinUtil.findByPin(clk).wiringPi()));
		System.out.println(String.format(" | Din (11)|| #%02d  | %s | MOSI | GPIO_%02d | %02d       |",
				PinUtil.findByPin(mosi).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(mosi).pinName(), 10, " "),
				PinUtil.findByPin(mosi).gpio(),
				PinUtil.findByPin(mosi).wiringPi()));
		System.out.println(String.format(" | Dout(12)|| #%02d  | %s | MISO | GPIO_%02d | %02d       |",
				PinUtil.findByPin(miso).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(miso).pinName(), 10, " "),
				PinUtil.findByPin(miso).gpio(),
				PinUtil.findByPin(miso).wiringPi()));
		System.out.println(String.format(" | CS  (10)|| #%02d  | %s | CS   | GPIO_%02d | %02d       |",
				PinUtil.findByPin(cs).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(cs).pinName(), 10, " "),
				PinUtil.findByPin(cs).gpio(),
				PinUtil.findByPin(cs).wiringPi()));
		System.out.println(" +---------++------+------------+-----+----------+----------+");
		System.out.println("Raspberry Pi is the Master, MCP3008 is the Slave:");
		System.out.println("- Dout on the MCP3008 goes to MISO on the RPi");
		System.out.println("- Din on the MCP3008 goes to MOSI on the RPi");
		System.out.println("Pins on the MCP3008 are numbered from 1 to 16, beginning top left, counter-clockwise.");
		System.out.println("       +--------+ ");
		System.out.println(String.format("%s CH0 -+  1  16 +- Vdd ",  (knobChannel == 0 ? "*" : " ")));
		System.out.println(String.format("%s CH1 -+  2  15 +- Vref ", (knobChannel == 1 ? "*" : " ")));
		System.out.println(String.format("%s CH2 -+  3  14 +- aGnd ", (knobChannel == 2 ? "*" : " ")));
		System.out.println(String.format("%s CH3 -+  4  13 +- CLK ",  (knobChannel == 3 ? "*" : " ")));
		System.out.println(String.format("%s CH4 -+  5  12 +- Dout ", (knobChannel == 4 ? "*" : " ")));
		System.out.println(String.format("%s CH5 -+  6  11 +- Din ",  (knobChannel == 5 ? "*" : " ")));
		System.out.println(String.format("%s CH6 -+  7  10 +- CS ",   (knobChannel == 6 ? "*" : " ")));
		System.out.println(String.format("%s CH7 -+  8   9 +- dGnd ", (knobChannel == 7 ? "*" : " ")));
		System.out.println("       +--------+ ");

		// Compose mapping for PinUtil
		String[] map = new String[4];
		map[0] = String.valueOf(PinUtil.findByPin(clk).pinNumber()) + ":" + "CLK";
		map[1] = String.valueOf(PinUtil.findByPin(miso).pinNumber()) + ":" + "Dout";
		map[2] = String.valueOf(PinUtil.findByPin(mosi).pinNumber()) + ":" + "Din";
		map[3] = String.valueOf(PinUtil.findByPin(cs).pinNumber()) + ":" + "CS";

		PinUtil.print(map);

		MCPReader.initMCP(MCPReader.MCPFlavor.MCP3008, miso, mosi, clk, cs);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nShutting down.");
			go = false;
			synchronized (Thread.currentThread()) {
				Thread.currentThread().notify();
			}
		}, "Shutdown Hook"));

		// Reading loop
		while (go) {
			int knob = MCPReader.readMCP(knobChannel);
			int feedback = MCPReader.readMCP(feedbackChannel);

			if (knob != feedback) {  // Now we're talking!
				if (DEBUG) {
					System.out.println(String.format("Difference detected: knob=%d, feedback=%d", knob, feedback));
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
		MCPReader.shutdownMCP();
	}
}
