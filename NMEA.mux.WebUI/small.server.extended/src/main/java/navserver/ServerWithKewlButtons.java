package navserver;

/**
 * Shows how to add push buttons to interact with the NavServer
 * Buttons with click, double-click, long-click, and other combinations.
 * Uses a small screen (oled SSD1306, Nokia, etc)
 */

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import http.client.HTTPClient;
import navrest.NavServer;
import navserver.button.PushButtonMaster;
import nmea.forwarders.SSD1306Processor;
import utils.PinUtil;
import utils.StaticUtil;
import utils.TimeUtil;

import java.util.HashMap;

public class ServerWithKewlButtons extends NavServer {

	private Pin appPin;
	private Pin shiftPin;

	final static PushButtonMaster pbmOne = new PushButtonMaster();
	final static PushButtonMaster pbmShift = new PushButtonMaster();

	private SSD1306Processor oledForwarder = null;

	private static int serverPort = 9999;
	private String turnLoggingOnURL = "";
	private String turnLoggingOffURL = "";
	private String terminateMuxURL = "";

	// Action to take depending on the type of click.
	// Propagate the button events to the SSD1306Processor (simple clicks, up and down)
	// - Shft + LongClick on button one: Shutdown (confirm with double-click within 1 second)
	// DoubleClick on button one: Show local menu
	// TODO: Start & Stop logging: PUT /mux/mux-process/on /mux/mux-process/off
	private boolean shutdownRequested = false;
	private boolean displayingLocalMenu = false;

	private Runnable onClickOne = () -> {
		System.out.println(String.format(">> %sSingle click on button 1", (pbmShift.isPushed() ? "[Shft] + " : "")));
		if (!pbmShift.isPushed() && oledForwarder != null) {
			oledForwarder.onButtonUpPressed();
		}
	};
	private Runnable onDoubleClickOne = () -> {
		System.out.println(String.format(">> %sDouble click on button 1", (pbmShift.isPushed() ? "[Shft] + " : "")));
		if (shutdownRequested) {
			// Shutting down the server AND the machine.
			try {
				System.out.println("Shutting down");
				if (oledForwarder != null) {
					oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
					oledForwarder.displayLines(new String[]{"Shutting down!"});
				}
				try {
					HTTPClient.doPost(this.terminateMuxURL, new HashMap<>(), null);
					TimeUtil.delay(2_000L);
				} catch (Exception ex) {
					System.err.println("PUT failed:");
					ex.printStackTrace();
				}
				StaticUtil.shutdown();
			} catch (Throwable ex) {
				ex.printStackTrace();
			} finally {
				if (oledForwarder != null) {
					oledForwarder.setExternallyOwned(false);
				}
			}
		} else { // Display menu?
			displayingLocalMenu = true;
			// TODO Do it...
		}
	};
	private Runnable onLongClickOne = () -> {
		System.out.println(String.format(">> %sLong click on button 1", (pbmShift.isPushed() ? "[Shft] + " : "")));
		if (pbmShift.isPushed()) { // Shift + LongClick on button one
			if (oledForwarder != null) {
				oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
				oledForwarder.displayLines(new String[] { "Shutting down...", "Confirm with",  "double-click", "within 1s"});
			} else {
				System.out.println("Shutting down, confirm with double-click");
			}
			shutdownRequested = true;
			Thread waiter = new Thread(() -> {
				try {
					this.wait(1_000L);
					shutdownRequested = false;
					if (oledForwarder != null) {
						oledForwarder.setExternallyOwned(false);
					}
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			});
			waiter.start();
		}
	};
	private Runnable onClickTwo = () -> {
		System.out.println(String.format(">> %sSingle click on button 2", (pbmOne.isPushed() ? "[Shft] + " : "")));
		if (!pbmOne.isPushed() && oledForwarder != null) {
			oledForwarder.onButtonDownPressed();
		}
	};
	private Runnable onDoubleClickTwo = () -> {
		System.out.println(String.format(">> %sDouble click on button 2", (pbmOne.isPushed() ? "[Shft] + " : "")));
	};
	private Runnable onLongClickTwo = () -> {
		System.out.println(String.format(">> %sLong click on button 2", (pbmOne.isPushed() ? "[Shft] + " : "")));
	};
	/**
	 *  For the Shift button, no operation needed. We only need if it is up or down.
	 *  See {@link PushButtonMaster#isPushed()}
	 */
	public ServerWithKewlButtons() {

		super(); // NavServer

		this.turnLoggingOnURL = String.format("http://localhost:%d/mux/mux-process/on", serverPort);
		this.turnLoggingOffURL = String.format("http://localhost:%d/mux/mux-process/off", serverPort);
		this.terminateMuxURL = String.format("http://localhost:%d/mux/terminate", serverPort);

		System.out.println(String.format("To turn logging ON, user PUT %s", this.turnLoggingOnURL));
		System.out.println(String.format("To turn logging OFF, user PUT %s", this.turnLoggingOffURL));
		System.out.println(String.format("To terminate the multiplexer, user POST %s", this.terminateMuxURL));

		System.out.println(String.format("Also try http://localhost:%d/zip/index.html from a browser", serverPort));
		System.out.println(String.format("     and http://localhost:%d/zip/extra/runner.html ", serverPort));

		try {
			// Provision buttons here
			appPin = RaspiPin.GPIO_29;  // Physical #40.
			shiftPin = RaspiPin.GPIO_28;// Physical #38.

			// Change pins, based on system properties. Use physical pin numbers.
			try {
				// Identified by the PHYSICAL pin numbers
				String buttonOnePinStr = System.getProperty("buttonOne", "40"); // GPIO_29
				String buttonTwoPinStr = System.getProperty("buttonTwo", "38"); // GPIO_28

				appPin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonOnePinStr));
				shiftPin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonTwoPinStr));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			pbmOne.update(
					"App-Button",
					appPin,
					onClickOne,
					onDoubleClickOne,
					onLongClickOne);

			pbmShift.update(
					"Shift-Button",
					shiftPin,
					onClickTwo,
					onDoubleClickTwo,
					onLongClickTwo);

		} catch (Throwable error) {
			error.printStackTrace();
		}

		System.out.println(">> Buttons provisioned!");

		// Was the SSD1306 loaded? This is loaded by the properties file.
		// Use the SSD1306Processor, SPI version.
		oledForwarder = SSD1306Processor.getInstance();
		// Following block just for tests.
		if (oledForwarder == null) {
			System.out.println("SSD1306 was NOT loaded");
		} else {
			System.out.println("SSD1306 was loaded!");
			// Now let's write in the screen...
			TimeUtil.delay(20_000L);
			System.out.println("Taking ownership on the screen");
			oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
			TimeUtil.delay(1_000L);
//			oledForwarder.displayLines(new String[] { "Taking ownership", "on the screen"});
			oledForwarder.displayLines(new String[] { "Shutting down...", "Confirm with",  "double-click", "within 1s"});
			TimeUtil.delay(5_000L);
			oledForwarder.displayLines(new String[] { "Releasing the screen"});
			TimeUtil.delay(2_000L);
			System.out.println("Releasing ownership on the screen");
			oledForwarder.setExternallyOwned(false); // Releasing ownership on the screen
		}
		if (false) { // Test REST requests
			TimeUtil.delay(5_000L);
			System.out.println("Killing the mux");
			try {
				HTTPClient.doPost(this.terminateMuxURL, new HashMap<>(), null);
			} catch (Exception ex) {
				System.err.println("PUT failed:");
				ex.printStackTrace();
			}
		}
		System.out.println("Nav Server fully initialized");
	}

	public static void freeResources() {
		// Cleanup
		pbmOne.freeResources();
		pbmShift.freeResources();
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
		}));
		new ServerWithKewlButtons();
	}

}
