package mux;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import http.client.HTTPClient;
import nmea.forwarders.SSD1306Processor;
import nmea.forwarders.SSD1306_HDMDisplay;
import nmea.mux.GenericNMEAMultiplexer;
import utils.PinUtil;
import utils.StaticUtil;
import utils.TCPUtils;
import utils.TimeUtil;
import utils.gpio.PushButtonController;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Shows how to add one push-button to interact with the NavServer
 * Uses a small screen (oled SSD1306, Nokia, etc)
 *
 * This class use making use of Runnable and Consumers (thank you Java 8).
 *
 * System properties:
 * - button.verbose, default false
 * - buttonOne, default 38 (Physical pin #)
 * - http.port, default 9999
 *
 */
public class MultiplexerWithOneButton extends GenericNMEAMultiplexer {

	private static boolean buttonVerbose = "true".equals(System.getProperty("button.verbose"));

	private SSD1306_HDMDisplay oledForwarder = null;

	private Pin buttonOnePin;

	final static PushButtonController buttonOne = new PushButtonController();

	private static int serverPort = 9999;

	private String turnLoggingOnURL = "";
	private String turnLoggingOffURL = "";
	private String terminateMuxURL = "";

	// Action to take depending on the type of click.
	// Propagate the button events to the SSD1306Processor (simple clicks, up and down)
	// DoubleClick on button: shutdown the system

	/* ----- Buttons Runnables (actions) ----- */
	private Runnable onClick = () -> {
		if (oledForwarder != null) {
			oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
			try {
				oledForwarder.displayLines(new String[]{"Doudble-click", "to shut down."});
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			oledForwarder.setExternallyOwned(false);
		}
	};

	private Runnable onDoubleClick = () -> {
		// Shutting down the server AND the machine.
		try {
			System.out.println("Shutting down");
			if (oledForwarder != null) {
				try {
					oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
					oledForwarder.displayLines(new String[]{"Shutting down!"});
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			try {
				System.out.println("Shutting down the MUX");
				try {
					HTTPClient.doPost(this.terminateMuxURL, new HashMap<>(), null);
				} catch (Exception wasDownAlready) {
					// I know...
				}
				System.out.println("Killing the box");
				TimeUtil.delay(2_000L);
			} catch (Exception ex) {
				System.err.println("Shutdown failed:");
				ex.printStackTrace();
			}
			if (oledForwarder != null) {
				StaticUtil.shutdown();
			} else {
				System.out.println("...Actually not killing the box.");
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		} finally {
			if (oledForwarder != null) {
				oledForwarder.setExternallyOwned(false);
			}
		}
	};
	/* ---- End of Buttons Runnables ---- */

	/**
	 *  Now using all the above.
	 *  Notice the call made to {@link SSD1306Processor#setSimulatorKeyPressedConsumer(Consumer)}
	 *  and {@link SSD1306Processor#setSimulatorKeyReleasedConsumer(Consumer)}
	 *  They implement the buttons simulators.
	 */
	public MultiplexerWithOneButton(Properties muxProps) {

		super(muxProps); // GenericNMEAMultiplexer
		System.out.println(">> Starting extension (after super())...");

		this.turnLoggingOnURL = String.format("http://localhost:%d/mux/mux-process/on", serverPort);
		this.turnLoggingOffURL = String.format("http://localhost:%d/mux/mux-process/off", serverPort);
		this.terminateMuxURL = String.format("http://localhost:%d/mux/terminate", serverPort);

		System.out.println(String.format("To turn logging ON, use PUT %s", this.turnLoggingOnURL));
		System.out.println(String.format("To turn logging OFF, use PUT %s", this.turnLoggingOffURL));
		System.out.println(String.format("To terminate the multiplexer, use POST %s", this.terminateMuxURL));

		System.out.println(String.format("\nREST Operations: GET http://localhost:%d/mux/oplist\n", serverPort));

		List<String[]> addresses = TCPUtils.getIPAddresses(true);
		String machineName = "localhost";
		if (addresses.size() == 1) {
			machineName = addresses.get(0)[1];
		}
		StringBuffer sb = new StringBuffer();
		System.out.println("IP addresses for localhost:");
		addresses.forEach(pair -> {
			System.out.println(String.format("%s -> %s", pair[0], pair[1]));
			// for tests
			if (pair[1].startsWith("192.168.")) { // ...a bit tough. I know.
				sb.append(pair[1]);
			}
		});
		if (sb.length() > 0) {
			machineName = sb.toString();
		}
		System.out.println(String.format("Also try http://%s:%d/web/index.html from a browser", machineName, serverPort));

		try {
			// Provision buttons here
			buttonOnePin = RaspiPin.GPIO_28; // Physical #38.

			// Change pins, based on system properties.
			// Use physical pin numbers.
			try {
				// Identified by the PHYSICAL pin numbers
				String buttonOnePinStr = System.getProperty("buttonOne", String.valueOf(PinUtil.getPhysicalByWiringPiNumber(buttonOnePin))); // GPIO_28

				buttonOnePin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonOnePinStr));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			// Pin mapping display for info
			String[] map = new String[12];
			int i = 0;
			map[i++] = String.valueOf(PinUtil.findByPin(buttonOnePin).pinNumber()) + ":Button Hot Wire";

			map[i++] = String.valueOf(PinUtil.GPIOPin.PWR_1.pinNumber())   + ":3v3";
			map[i++] = String.valueOf(PinUtil.GPIOPin.PWR_2.pinNumber())   + ":5v0";

			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_15.pinNumber()) + ":Tx";
			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_16.pinNumber()) + ":Rx";

			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_14.pinNumber()) + ":Clock";
			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_12.pinNumber()) + ":Data"; // Aka MOSI. Slave is the Screen, Master the RPi
			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_10.pinNumber()) + ":CS";
			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_5.pinNumber())  + ":Rst";
			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_4.pinNumber())  + ":DC";

			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_8.pinNumber())  + ":SDA";
			map[i++] = String.valueOf(PinUtil.GPIOPin.GPIO_9.pinNumber())  + ":SLC";

			System.out.println("---------------------------- P I N   M A P P I N G ------------------------------------------");
			PinUtil.print(map);
			System.out.println("> Screen is powered with 5V, button by 3v3 or 5V, 3v3 is used by the Compass.");
			System.out.println("---------------------------------------------------------------------------------------------\n");

			// Button provisioning, with its operations
			buttonOne.update(
					"The-Button",
					buttonOnePin,
					onClick,
					onDoubleClick,
					null); // null: unchanged

		} catch (Throwable error) {
			error.printStackTrace();
		}

		System.out.println(">> Button is provisioned");

		// Was the SSD1306 loaded? This is loaded by the properties file.
		// Use the SSD1306Processor, SPI version.
		oledForwarder = SSD1306_HDMDisplay.getInstance(); // A singleton.
		if (oledForwarder == null) {
			System.out.println("SSD1306 was NOT loaded");
		} else {
			boolean simulating = oledForwarder.isSimulating();

			final int SHFT_KEY = 16,
								CTRL_KEY = 17;

			System.out.println(String.format("SSD1306 was loaded! (%s)", simulating ? "simulating" : "for real"));
			if (simulating) {
				// Simulator led color
				oledForwarder.setSimutatorLedColor(Color.WHITE);
				// Seems not to be possible to have left shift and right shift. When one is on, the other is ignored.
				// Buttons simulator
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				System.out.println(">> Simulating buttons: Button 1: Shift                ");
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				// Runnables for the simulator
				oledForwarder.setSimulatorKeyPressedConsumer((keyEvent) -> {
					System.out.println("KeyPressed:" + keyEvent);
					if (keyEvent.getKeyCode() == SHFT_KEY) {
						System.out.println("Button pushed");
						buttonOne.manageButtonState(PushButtonController.ButtonStatus.HIGH);
					}
				});
				oledForwarder.setSimulatorKeyReleasedConsumer((keyEvent) -> {
					System.out.println("KeyReleased:" + keyEvent);
					if (keyEvent.getKeyCode() == SHFT_KEY) {
						System.out.println("Button released");
						buttonOne.manageButtonState(PushButtonController.ButtonStatus.LOW);
					}
				});
			}
		}
		System.out.println("Nav Server fully initialized");
	}

	public static void freeResources() {
		// Cleanup
		buttonOne.freeResources();
	}

	public static void main(String... args) {

		try {
			serverPort = Integer.parseInt(System.getProperty("http.port", String.valueOf(serverPort)));
		} catch (NumberFormatException nfe) {
			System.err.println("Ooops");
			nfe.printStackTrace();
		}
		System.out.println(String.format(">>> Server port is %d", serverPort));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			freeResources();
		}, "Shutdown Hook"));
		Properties definitions = GenericNMEAMultiplexer.getDefinitions();

		boolean startProcessingOnStart = "true".equals(System.getProperty("process.on.start", "true"));
		MultiplexerWithOneButton mux = new MultiplexerWithOneButton(definitions);
		mux.setEnableProcess(startProcessingOnStart);
		// with.http.server=yes
		// http.port=9999
		if ("yes".equals(definitions.getProperty("with.http.server", "no"))) {
			mux.startAdminServer(Integer.parseInt(definitions.getProperty("http.port", "9999")));
		}
	}

}
