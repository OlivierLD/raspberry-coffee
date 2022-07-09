package raspiradar;

import com.pi4j.io.i2c.I2CFactory;
import gnu.io.CommPortIdentifier;
import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;
import utils.PinUtil;
import utils.TimeUtil;
import utils.gpio.StringToGPIOPin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Connect another machine with a USB cable.<br/>
 * Serial port (ttyUSB0 below) may vary.<br/>
 *<br/>
 * See system properties:
 * <ul>
 * <li><code>"serial.port"</code>, default <code>"/dev/ttyUSB0"</code></li>
 * <li><code>"baud.rate"</code>, default <code>"9600"</code></li>
 * </ul>
 *
 * Read & write (mostly) to the Serial Port
 *
 * This is an illustration of the way to use the {@link RasPiRadar}, and forward the data to a serial port
 */
public class RasPiSerialRadar implements SerialIOCallbacks {

	private static boolean verbose = "true".equals(System.getProperty("radar.verbose"));

	private static final int BUFFER_LENGTH = 10;
	private static List<Double> buffer = new ArrayList<>(BUFFER_LENGTH);


	@Override
	public void connected(boolean b) {}

	@Override
	public void onSerialData(byte b) {}

	@Override
	public void onSerialData(byte[] ba, int len) {}

	private SerialCommunicator sc;

	private void initSerialComm() {
		sc = new SerialCommunicator(this);
		sc.setVerbose(verbose);

		Map<String, CommPortIdentifier> pm = sc.getPortList();
		Set<String> ports = pm.keySet();
		if (ports.size() == 0) {
			System.out.println("No serial port found.");
			System.out.println("Did you run as administrator (sudo) ?");
		}
		System.out.println("== Serial Port List ==");
		for (String port : ports) {
			System.out.println("-> " + port);
		}
		System.out.println("======================");

		String serialPortName = System.getProperty("serial.port", "/dev/ttyUSB0");
		String baudRateStr = System.getProperty("baud.rate", "9600");
		System.out.println(String.format("Opening port %s:%s", serialPortName, baudRateStr));
		CommPortIdentifier serialOutPort = pm.get(serialPortName);
		if (serialOutPort == null) {
			System.out.println(String.format("Port %s not found, aborting", serialPortName));
			System.exit(1);
		}
		try {
			sc.connect(serialOutPort, "RadarOut", Integer.parseInt(baudRateStr));
			boolean b = sc.initIOStream();
			System.out.println("IO Streams " + (b ? "" : "NOT ") + "initialized");
			sc.initListener();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void serialOutput(String sentence) throws IOException {
		sc.writeData(sentence + "\n");
	}

	private void shutdownSerialComm() {
		try {
			sc.disconnect();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private final static String PCA9685_SERVO_PORT = "--servo-port:";
	private final static String DELAY              = "--delay:";
	private final static String TRIGGER_PIN        = "--trigger-pin:";
	private final static String ECHO_PIN           = "--echo-pin:";
	private final static String JUST_RESET         = "--just-reset";
	private final static String JUST_ONE_LOOP      = "--just-one-loop";

	private final static String HELP               = "--help";

	private static boolean loop = true;
	private static long delay = 100L;
	private static boolean justReset = false;
	private static boolean justOneLoop = false;

	private static void displayHelp() {
		System.out.println("Program parameters are:");
		System.out.println(String.format("\t%sXX, default is %s, values are in [0..15]", PCA9685_SERVO_PORT, "0"));
		System.out.println(String.format("\t%sXX, default is %s ms, wait time between measurements", DELAY, "100"));
		System.out.println(String.format("\t%sXX, default is 16, PHYSICAL number of the trigger pin, range is [1..40]", TRIGGER_PIN));
		System.out.println(String.format("\t%sXX, default is 18, PHYSICAL number of the echo pin, range is [1..40]", ECHO_PIN));
		System.out.println(String.format("\t%s, just reset the servo and exit", JUST_RESET));
		System.out.println(String.format("\t%s, does just one loop (0, -90, +90, 0) and exits", JUST_ONE_LOOP));
		System.out.println(String.format("\t%s, you're on it. Exiting.", HELP));
	}

	public static void main(String... args) {

		if (Arrays.stream(args).filter(prm -> HELP.equals(prm)).findFirst().isPresent()) {
			displayHelp();
			System.exit(0);
		}

		Consumer<RasPiRadar.DirectionAndRange> defaultDataConsumer = (data) -> {
			buffer.add(data.range());
			while (buffer.size() > BUFFER_LENGTH) {
				buffer.remove(0);
			}
			double avg = buffer.stream().mapToDouble(d -> d).average().getAsDouble();
			System.out.println(String.format("Default (static) RasPiRadar Consumer >> Bearing %s%02d, distance %.02f cm", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), avg));
		};

		int servoPort  = 0;

		Integer trig = null, echo = null;

		// User prms
		for (String str : args) {
			if (str.startsWith(PCA9685_SERVO_PORT)) {
				String s = str.substring(PCA9685_SERVO_PORT.length());
				servoPort = Integer.parseInt(s);
			}
			if (str.startsWith(DELAY)) {
				String s = str.substring(DELAY.length());
				delay = Long.parseLong(s);
			}
			// Trig & Echo pin PHYSICAL numbers (1..40)
			if (str.startsWith(TRIGGER_PIN)) {
				String s = str.substring(TRIGGER_PIN.length());
				trig = Integer.parseInt(s);
			}
			if (str.startsWith(ECHO_PIN)) {
				String s = str.substring(ECHO_PIN.length());
				echo = Integer.parseInt(s);
			}
			if (str.equals(JUST_RESET)) {
				justReset = true;
			}
			if (str.equals(JUST_ONE_LOOP)) {
				justOneLoop = true;
			}
		}
		if (echo != null ^ trig != null) {
			throw new RuntimeException("Echo & Trigger pin numbers must be provided together, or not at all.");
		}

		System.out.println(String.format("Driving Servo on Channel %d", servoPort));
		System.out.println(String.format("Wait when scanning %d ms", delay));

		RasPiSerialRadar serialRadar = new RasPiSerialRadar();
		serialRadar.initSerialComm();
		RasPiRadar rpr = null;
		try {
			if (echo == null && trig == null) {
				rpr = new RasPiRadar(true, servoPort);
			} else {
				rpr = new RasPiRadar(true, servoPort,
						StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(trig)),
						StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(echo)));
			}
		} catch (I2CFactory.UnsupportedBusNumberException | UnsatisfiedLinkError notOnAPi) {
			System.out.println("Not on a Pi? Moving on...");
		}

		if (verbose && rpr != null && rpr.getHcSR04() != null) {
			System.out.println("HC-SR04 & Serial wiring:");
			String[] map = new String[4];
			map[0] = String.valueOf(PinUtil.findByPin(rpr.getHcSR04().getTrigPin().getName()).pinNumber()) + ":" + "Trigger";
			map[1] = String.valueOf(PinUtil.findByPin(rpr.getHcSR04().getEchoPin().getName()).pinNumber()) + ":" + "Echo";

			map[2] = String.valueOf(8) + ":" + "TX, white";
			map[3] = String.valueOf(10) + ":" + "RX, green";

			PinUtil.print(map);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> loop = false, "Shutdown Hook"));

		// Will take care of sending data to the serial port.
		rpr.setDataConsumer(data -> {
			// Injected Consumer -> CSV: direction;range\n
			buffer.add(data.range());
			while (buffer.size() > BUFFER_LENGTH) {
				buffer.remove(0);
			}
			double avg = buffer.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
			String serialSentence = String.format("%s%02d;%.02f", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), avg);
			if (verbose) {
				System.out.println(String.format("Emitting [%s]", serialSentence));
			}
			try {
				serialRadar.serialOutput(serialSentence);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		});
		// For simulation, override if needed
//	rpr.setRangeSimulator(RasPiRadar::simulateUserRange);

		try {
			if (rpr != null) {
				rpr.stop(); // init
			}
			if (justReset) {
				loop = false;
				TimeUtil.delay(1_000L);
			}

			int inc = 1;
			int bearing = 0;
			double dist = 0;
			int hitExtremity = 0;
			while (loop) {
				try {
					if (rpr != null) {
						rpr.setAngle(bearing);
						// Measure distance here, broadcast it witdouble dist = h bearing.
						dist = rpr.readDistance();
						// Consumer
						rpr.consumeData(new RasPiRadar.DirectionAndRange(bearing, dist));
					} else { // For dev...
						defaultDataConsumer.accept(new RasPiRadar.DirectionAndRange(bearing, dist));
					}
					if (justOneLoop && hitExtremity == 2 && bearing == 0) {
						loop = false;
					}

					bearing += inc;
					if (bearing > 90 || bearing < -90) { // then flip
						hitExtremity += 1;
						inc *= -1;
						bearing += (2 * inc);
					}
					// Sleep here
					TimeUtil.delay(delay);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} finally {
			if (rpr != null) {
				rpr.stop();
				TimeUtil.delay(1_000L); // Before freeing, get some time to get back to zero.
				rpr.free();
			}
			serialRadar.shutdownSerialComm();
		}
		System.out.println("Done.");
	}
}
