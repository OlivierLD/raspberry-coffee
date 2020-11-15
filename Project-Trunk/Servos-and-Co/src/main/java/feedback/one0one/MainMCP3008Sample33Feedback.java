package feedback.one0one;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import utils.PinUtil;
import utils.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.Function;

import static utils.StringUtils.lpad;

/**
 * To measure a voltage, between 0 and 3.3V or 5.0V (See -DvRef= )
 * And deduct an angle.
 */
public class MainMCP3008Sample33Feedback {

	private final static boolean DEBUG = "true".equals(System.getProperty("debug", "false"));
	private final static boolean CALIBRATION = "true".equals(System.getProperty("calibration", "false"));

	private static double vRef = 3.3;
	static {
		vRef = Double.parseDouble(System.getProperty("vRef", String.valueOf(vRef)));
	}

	private static boolean go = true;
	private static int adcChannel =
					MCPReader.MCP3008InputChannels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

	private static final String MISO_PRM_PREFIX = "--miso:";
	private static final String MOSI_PRM_PREFIX = "--mosi:";
	private static final String CLK_PRM_PREFIX  =  "--clk:";
	private static final String CS_PRM_PREFIX   =   "--cs:";

	private static final String CHANNEL_PREFIX  = "--channel:";

	private static final String MINUS_90_PREFIX = "--minus90:";
	private static final String PLUS_90_PREFIX = "--plus90:";

	private static Function<Integer, Double> adcToDegTransformer = x -> (((x / 1023.0) * 300d) - (300d / 2)); // Default behavior

	private static WebSocketClient webSocketClient = null;

	private static void initWSConnection(String serverURI) {
		System.out.println(String.format("Try to connect on %s", serverURI));
		try {
			webSocketClient = new WebSocketClient(new URI(serverURI)) { // , (Draft) null) {
				@Override
				public void onOpen(ServerHandshake serverHandshake) {
					// TODO Implement this method
				}

				@Override
				public void onMessage(String string) {
					// TODO Implement this method
				}

				@Override
				public void onClose(int i, String string, boolean b) {
					// TODO Implement this method
				}

				@Override
				public void onError(Exception exception) {
					// TODO Implement this method
				}
			};
			boolean b = webSocketClient.connectBlocking();
			System.out.println(String.format("WS Connection on %s OK: %s.", serverURI, b));
		} catch (Exception ex) {
			System.err.println("WebSocket connection:");
			ex.printStackTrace();
		}
	}

	public static void main(String... args) {

		// Default pins
		Pin miso = PinUtil.GPIOPin.GPIO_13.pin();
		Pin mosi = PinUtil.GPIOPin.GPIO_12.pin();
		Pin clk  = PinUtil.GPIOPin.GPIO_14.pin();
		Pin cs   = PinUtil.GPIOPin.GPIO_10.pin();

		System.out.println(String.format("Usage is java %s %s%d %s%d %s%d %s%d %s%d",
				MainMCP3008Sample33Feedback.class.getName(),       // <- WhoooAhhhaahahha!
				MISO_PRM_PREFIX,  PinUtil.findByPin(miso).gpio(),
				MOSI_PRM_PREFIX,  PinUtil.findByPin(mosi).gpio(),
				CLK_PRM_PREFIX,   PinUtil.findByPin(clk).gpio(),
				CS_PRM_PREFIX,    PinUtil.findByPin(cs).gpio(),
				CHANNEL_PREFIX,   adcChannel));
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
				} else if (prm.startsWith(CHANNEL_PREFIX)) {
					String chValue = prm.substring(CHANNEL_PREFIX.length());
					try {
						adcChannel = Integer.parseInt(chValue);
						if (adcChannel > 7 || adcChannel < 0) {
							throw new RuntimeException("Channel in [0..7] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(MINUS_90_PREFIX) || prm.startsWith(PLUS_90_PREFIX)) {
					if (!CALIBRATION) {
						System.err.println(String.format("%s or %s are not required for calibration", MINUS_90_PREFIX, PLUS_90_PREFIX));
					}
				} else {
					// What?
					System.err.println(String.format("Un-managed prm: %s", prm));
				}
			}
		}

		System.out.println(String.format("Reading MCP3008 on channel %d", adcChannel));
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
		System.out.println(String.format("%s CH0 -+  1  16 +- Vdd ",  (adcChannel == 0 ? "*" : " ")));
		System.out.println(String.format("%s CH1 -+  2  15 +- Vref ", (adcChannel == 1 ? "*" : " ")));
		System.out.println(String.format("%s CH2 -+  3  14 +- aGnd ", (adcChannel == 2 ? "*" : " ")));
		System.out.println(String.format("%s CH3 -+  4  13 +- CLK ",  (adcChannel == 3 ? "*" : " ")));
		System.out.println(String.format("%s CH4 -+  5  12 +- Dout ", (adcChannel == 4 ? "*" : " ")));
		System.out.println(String.format("%s CH5 -+  6  11 +- Din ",  (adcChannel == 5 ? "*" : " ")));
		System.out.println(String.format("%s CH6 -+  7  10 +- CS ",   (adcChannel == 6 ? "*" : " ")));
		System.out.println(String.format("%s CH7 -+  8   9 +- dGnd ", (adcChannel == 7 ? "*" : " ")));
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
			if (webSocketClient != null && webSocketClient.isOpen()) {
				webSocketClient.close();
			}
		}, "Shutdown Hook"));
		int lastRead = 0;
		int tolerance = 1; // 5; // TODO Make this a variable?
		boolean first = true;

		/*
		 * By default:
		 *  50% will be 0 degree
		 *   0% will be -150 degrees,
		 * 100% will be 150 degrees.
		 */

		if (CALIBRATION) {
			System.out.println("- Rotate the potentiometer to find the position where Volume is 50%");
			System.out.println("- Then move 90 degrees clockwise, and note the ADC value XXXX.");
			System.out.println("- Then move 90 degrees counter-clockwise, and note the ADC value YYYY.");
			System.out.println("You will use those values in the real world, using the runtime arguments --minus90:XXXX and --plus90:YYYY");
		} else {
			int minus90AdcValue = -1,
					plus90AdcValue = -1;
			OptionalInt minus90 = Arrays.stream(args)
					.filter(arg -> arg.startsWith(MINUS_90_PREFIX))
					.mapToInt(arg -> Integer.parseInt(arg.substring(MINUS_90_PREFIX.length())))
					.findFirst();
			if (minus90.isPresent()) {
				minus90AdcValue = minus90.getAsInt();
				// Check if in [0..1023]
				if (minus90AdcValue < 0 || minus90AdcValue > 1023) {
					throw new IllegalArgumentException(String.format("Bad value for %s, %d not in [0..1023]", MINUS_90_PREFIX, minus90AdcValue));
				}
			} else {
				throw new IllegalArgumentException(String.format("%s required if not in Calibration mode", MINUS_90_PREFIX));
			}
			OptionalInt plus90 = Arrays.stream(args)
					.filter(arg -> arg.startsWith(PLUS_90_PREFIX))
					.mapToInt(arg -> Integer.parseInt(arg.substring(PLUS_90_PREFIX.length())))
					.findFirst();
			if (plus90.isPresent()) {
				plus90AdcValue = plus90.getAsInt();
				// Check if in [0..1023]
				if (plus90AdcValue < 0 || plus90AdcValue > 1023) {
					throw new IllegalArgumentException(String.format("Bad value for %s, %d not in [0..1023]", PLUS_90_PREFIX, plus90AdcValue));
				}
			} else {
				throw new IllegalArgumentException(String.format("%s required if not in Calibration mode", PLUS_90_PREFIX));
			}
			if (plus90AdcValue < minus90AdcValue) {
				throw new IllegalArgumentException(String.format("Bad values [%d, %d], min >= max", minus90AdcValue, plus90AdcValue));
			}
			// Full range elaboration here, f(x) = a*x + b, 1st degree function (linear, hey).
			double coeffA = 180d / (double)(plus90AdcValue - minus90AdcValue);
			double coeffB = 90d - (plus90AdcValue * coeffA);
			System.out.println(String.format("Function coefficients for f(x) = a.x + b: \n\ta: %f,\n\tb:%f", coeffA, coeffB));
			Function<Integer, Double> adcToDegrees = x -> (coeffA * x + coeffB);
			System.out.println("\nParameter validation:");
			System.out.println(String.format("ADC=%04d -> %f\272", 512, adcToDegrees.apply(512)));
			System.out.println(String.format("ADC=%04d -> %f\272", minus90AdcValue, adcToDegrees.apply(minus90AdcValue)));
			System.out.println(String.format("ADC=%04d -> %f\272", plus90AdcValue, adcToDegrees.apply(plus90AdcValue)));
			// ADC values for 0 and 1023 (extrema)
			System.out.println(String.format("ADC=%04d -> %f\272", 0, adcToDegrees.apply(0)));
			System.out.println(String.format("ADC=%04d -> %f\272", 1023, adcToDegrees.apply(1023)));
			// Done!
			adcToDegTransformer = adcToDegrees;

			// WebSockets?
			String wsURI = System.getProperty("ws.uri");
			if (wsURI != null) {
				initWSConnection(wsURI);
			}
		}

		// Reading loop
		while (go) {
	//	System.out.println("Reading channel " + adcChannel);
			int adc = MCPReader.readMCP(adcChannel);
	//	System.out.println(String.format("From ch %d: %d", adcChannel, adc));
			int postAdjust = Math.abs(adc - lastRead);
			if (first || postAdjust > tolerance) {
				double volume = (adc / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				if (DEBUG) {
					System.out.println("readAdc:" + Integer.toString(adc) +
							" (0x" + lpad(Integer.toString(adc, 16).toUpperCase(), 2, "0") +
							", 0&" + lpad(Integer.toString(adc, 2), 8, "0") + ")");
				}
				if (CALIBRATION) {
					System.out.println(String.format("Volume: %05.01f%% (ADC: %04d) => %.03f V.",
							volume,
							adc,
							(vRef * (adc / 1023.0))));  // Volts
				} else {
					double deviceAngle = adcToDegTransformer.apply(adc);
					try {
						System.out.println(String.format("Volume: %05.01f%% (%04d) => %.03f V, %+06.02f degree(s)",
								volume,
								adc,
								(vRef * (adc / 1023.0)),  // Volts
								deviceAngle));            // Angle, centered (default on 300 degrees range)
						// If WebSocket Server exists
						if (webSocketClient != null) {
							if (!webSocketClient.isOpen()) {
								System.out.println("WS, Connecting before send...");
								webSocketClient.connectBlocking();
							}
							webSocketClient.send(Double.toString(deviceAngle)); // Push message
						}

					} catch (Exception whatever) {
						whatever.printStackTrace();
						System.out.println("Volume :" + volume +
								"\nADC:" + adc +
								"\nVolts:" + (vRef * (adc / 1023.0)) +
								"\nAngle:" + deviceAngle);
					}
				}
				lastRead = adc;
				first = false;
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
