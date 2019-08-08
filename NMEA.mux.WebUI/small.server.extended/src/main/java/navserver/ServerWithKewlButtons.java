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

	private static boolean buttonVerbose = "true".equals(System.getProperty("button.verbose"));

	private Runnable pauseLogging = () -> {
		try {
			HTTPClient.doPut(this.turnLoggingOffURL, new HashMap<>(), null);
		} catch (Exception ex) {
			System.err.println("Pausing logging:");
			ex.printStackTrace();
		}
	};

	private Runnable resumeLogging = () -> {
		try {
			HTTPClient.doPut(this.turnLoggingOnURL, new HashMap<>(), null);
		} catch (Exception ex) {
			System.err.println("Resuming logging:");
			ex.printStackTrace();
		}
	};

	private class MenuItem {
		private String title;
		private Runnable action;

		public MenuItem() {}
		public MenuItem title(String title) {
			this.title = title;
			return this;
		}
		public MenuItem action(Runnable action) {
			this.action = action;
			return this;
		}
		public String getTitle() { return this.title; }
		public Runnable getAction() { return this.action; }
	}

	private MenuItem[] localMenuItems = new MenuItem[] {
		new MenuItem().title("Pause logging").action(pauseLogging),
		new MenuItem().title("Resume logging").action(resumeLogging)
	};
	private int localMenuItemIndex = 0;

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
	// DoubleClick on button two: Screen Saver mode. Any simple-click to resume.
	// In the local menu: Start & Stop logging: PUT /mux/mux-process/on /mux/mux-process/off
	private boolean shutdownRequested = false;
	private boolean displayingLocalMenu = false;
	private boolean screenSaverMode = false;
	private Thread screenSaverThread = null;

	private void displayLocalMenuItems() {
		if (oledForwarder != null) {
			oledForwarder.displayLines(new String[]{
					"Up and down to Scroll",
					"--------------------",
					"- " + localMenuItems[localMenuItemIndex].getTitle(),
					"--------------------",
					"Db-clk 1: select",
					"Db-clk 2: cancel"
			});
		}
	}

	private void releaseScreenSaver() {
		if (buttonVerbose) {
			System.out.println("Releasing screen saver");
		}
		screenSaverMode = false;
		if (screenSaverThread != null) {
			synchronized (screenSaverThread) {
				screenSaverThread.notify();
			}
		}
		screenSaverThread = null;
	}

	private Runnable onClickOne = () -> {
		if (buttonVerbose) {
			System.out.println(String.format(">> %sSingle click on button 1", (pbmShift.isPushed() ? "[Shft] + " : "")));
		}
		if (screenSaverMode) {
			releaseScreenSaver();
		} else if (displayingLocalMenu) {
			// Previous menu item
			localMenuItemIndex += 1;
			if (localMenuItemIndex > (localMenuItems.length - 1)) {
				localMenuItemIndex = 0;
			}
			displayLocalMenuItems();
		} else if (!pbmShift.isPushed() && oledForwarder != null) {
			if (buttonVerbose) {
				System.out.println("1 up!");
			}
			oledForwarder.onButtonUpPressed();
		}
	};
	private Runnable onDoubleClickOne = () -> {
		if (buttonVerbose) {
			System.out.println(String.format(">> %sDouble click on button 1", (pbmShift.isPushed() ? "[Shft] + " : "")));
		}
		if (shutdownRequested) {
			// Shutting down the server AND the machine.
			try {
				System.out.println("Shutting down");
				if (oledForwarder != null) {
					oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
					oledForwarder.displayLines(new String[]{"Shutting down!"});
				}
				try {
					System.out.println("Shutting down the MUX");
					HTTPClient.doPost(this.terminateMuxURL, new HashMap<>(), null);
					System.out.println("Killing the box");
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
		} else if (displayingLocalMenu) {
			// Execute and return to normal mode
			localMenuItems[localMenuItemIndex].getAction().run();
			displayingLocalMenu = false;
			if (oledForwarder != null) {
				oledForwarder.setExternallyOwned(false); // Release
			}
		} else { // Display menu?
			if (buttonVerbose) {
				System.out.println("Displaying local menu items");
			}
			displayingLocalMenu = true;
			if (oledForwarder != null) {
				oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
				localMenuItemIndex = 0;
				displayLocalMenuItems();
			}
		}
	};
	private Runnable onLongClickOne = () -> {
		if (buttonVerbose) {
			System.out.println(String.format(">> %sLong click on button 1", (pbmShift.isPushed() ? "[Shft] + " : "")));
		}
		if (pbmShift.isPushed()) { // Shift + LongClick on button one
			if (oledForwarder != null) {
				oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
				oledForwarder.displayLines(new String[] { "Shutting down...", "Confirm with",  "double-click", "within 3s"});
			} else {
				System.out.println("Shutting down, confirm with double-click");
			}
			shutdownRequested = true;
			Thread waiter = new Thread(() -> {
				try {
					synchronized (this) {
						this.wait(3_000L);
					}
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
		if (buttonVerbose) {
			System.out.println(String.format(">> %sSingle click on button 2", (pbmOne.isPushed() ? "[Shft] + " : "")));
		}
		if (screenSaverMode) {
			releaseScreenSaver();
		} else if (displayingLocalMenu) {
			// Previous menu item
			localMenuItemIndex -= 1;
			if (localMenuItemIndex < 0) {
				localMenuItemIndex = (localMenuItems.length - 1);
			}
			displayLocalMenuItems();
		} else if (!pbmOne.isPushed() && oledForwarder != null) {
			if (buttonVerbose) {
				System.out.println("1 down!");
			}
			oledForwarder.onButtonDownPressed();
		}
	};
	private Runnable onDoubleClickTwo = () -> {
		if (buttonVerbose) {
			System.out.println(String.format(">> %sDouble click on button 2", (pbmOne.isPushed() ? "[Shft] + " : "")));
		}
		if (displayingLocalMenu) {
			// Cancel
			displayingLocalMenu = false;
			if (oledForwarder != null) {
				oledForwarder.setExternallyOwned(false); // Release
			}
		} else if (!screenSaverMode) {
			if (buttonVerbose) {
				System.out.println("Starting screen saver...");
			}
			screenSaverMode = true;
			if (oledForwarder != null) {
				oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
			}
			// Start thread
			screenSaverThread = new Thread(() -> {
				boolean on = true;
				while (screenSaverMode) {
					if (oledForwarder != null) {
						oledForwarder.displayLines(new String[]{String.format("%s", on ? "." : "")});
						on = !on;
						try {
							synchronized (this) {
								this.wait(1_000L);
							}
						} catch (InterruptedException ie) {
							// I know...
						}
					}
				}
				if (buttonVerbose) {
					System.out.println("Screen back to life");
				}
				if (oledForwarder != null) {
					oledForwarder.setExternallyOwned(false); // Releasing ownership
				}
			});
			screenSaverThread.start();
		} else {
			System.out.println("Already in screensaver mode");
		}
	};
	private Runnable onLongClickTwo = () -> {
		if (buttonVerbose) {
			System.out.println(String.format(">> %sLong click on button 2", (pbmOne.isPushed() ? "[Shft] + " : "")));
		}
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
		System.out.println(String.format("     and http://localhost:%d/zip/runner.html ", serverPort));

		// Help display here
		System.out.println("+---------------------------------------------------------------------------------------+");
		System.out.println("| Shft + LongClick on button one: Shutdown (confirm with double-click within 3 seconds) |");
		System.out.println("| DoubleClick on button one: Show local menu                                            |");
		System.out.println("| DoubleClick on button two: Screen Saver mode. Any simple-click to resume.             |");
		System.out.println("+---------------------------------------------------------------------------------------+");

		try {
			// Provision buttons here
			appPin = RaspiPin.GPIO_29;  // Physical #38.
			shiftPin = RaspiPin.GPIO_28;// Physical #40.

			// Change pins, based on system properties. Use physical pin numbers.
			try {
				// Identified by the PHYSICAL pin numbers
				String buttonOnePinStr = System.getProperty("buttonOne", "38"); // GPIO_28
				String buttonTwoPinStr = System.getProperty("buttonTwo", "40"); // GPIO_29

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
			TimeUtil.delay(10_000L);
			System.out.println("Taking ownership on the screen");
			oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
			TimeUtil.delay(500L);
//			oledForwarder.displayLines(new String[] { "Taking ownership", "on the screen"});
			oledForwarder.displayLines(new String[] { "Shutting down...", "Confirm with",  "double-click", "within 1s"});
			TimeUtil.delay(4_000L);

			oledForwarder.displayLines(new String[] {
					"Up and down to Scroll",
					"--------------------",
					"- Menu Operation",
					"--------------------",
					"Db-clk 1: select",
					"Db-clk 2: cancel"
			});
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
