package navserver;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import http.client.HTTPClient;
import navrest.NavServer;
import navserver.button.PushButtonController;
import nmea.forwarders.SSD1306Processor;
import utils.PinUtil;
import utils.StaticUtil;
import utils.TCPUtils;
import utils.TimeUtil;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

/**
 * Shows how to add push buttons to interact with the NavServer
 * Buttons with click, double-click, long-click, and other combinations.
 * Uses a small screen (oled SSD1306, Nokia, etc)
 *
 * This class use making use of Runnable.
 *
 * System properties:
 * - button.verbose, default false
 * - buttonOne, default 38 (Physical pin #)
 * - buttonTwo, default 40 (Physical pin #)
 * - http.port, default 9999
 *
 */
public class ServerWithKewlButtons extends NavServer {

	private static boolean buttonVerbose = "true".equals(System.getProperty("button.verbose"));

	private SSD1306Processor oledForwarder = null;

	// Local menu operations
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

	private Runnable terminateMux = () -> {
		try {
			HTTPClient.doPost(this.terminateMuxURL, new HashMap<>(), null);
		} catch (Exception ex) {
			System.err.println("Terminate Mux:");
			ex.printStackTrace();
		}
	};

	private Runnable sayHello = () -> {
		try {
			if (oledForwarder != null) {
				oledForwarder.displayLines(new String[]{ "Hello !" });
				TimeUtil.delay(4_000L);
			}
		} catch (Exception ex) {
			System.err.println("Say Hello:");
			ex.printStackTrace();
		}
	};

	/**
	 * MenuItem class
	 * Used to store the label and action of each item
	 * of the local menu.
	 */
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

	private MenuItem[] localMenuItems = new MenuItem[]{
			new MenuItem().title("Pause logging").action(pauseLogging),
			new MenuItem().title("Resume logging").action(resumeLogging),
			new MenuItem().title("Terminate Multiplexer").action(terminateMux),
			new MenuItem().title("Say Hello").action(sayHello)
	};
	private int localMenuItemIndex = 0;

	private Pin buttonOnePin; // Top
	private Pin buttonTwoPin; // Bottom

	final static PushButtonController buttonOne = new PushButtonController();
	final static PushButtonController buttonTwo = new PushButtonController();

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

	private void displayLocalMenu() {
		if (oledForwarder != null) {
			oledForwarder.displayLines(new String[]{
					"> Up and down to Scroll",
					"--------------------",
					localMenuItems[localMenuItemIndex].getTitle(),
					"--------------------",
					"> Db-clk 1: select",
					"> Db-clk 2: cancel"
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

	// Buttons Runnables
	private Runnable onClickOne = () -> {
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.println(String.format(">> %sSingle click on button 1", (buttonTwo.isPushed() ? "[Shft] + " : "")));
		}
		if (screenSaverMode) {
			releaseScreenSaver();
		} else if (displayingLocalMenu) {
			// Previous menu item
			localMenuItemIndex += 1;
			if (localMenuItemIndex > (localMenuItems.length - 1)) {
				localMenuItemIndex = 0;
			}
			displayLocalMenu();
		} else if (!buttonTwo.isPushed() && oledForwarder != null) {
			if (buttonVerbose) {
				System.out.println("1 up!");
			}
			oledForwarder.onButtonUpPressed();
		}
	};

	private Runnable onDoubleClickOne = () -> {
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.println(String.format(">> %sDouble click on button 1", (buttonTwo.isPushed() ? "[Shft] + " : "")));
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
				displayLocalMenu();
			}
		}
	};

	private Runnable onLongClickOne = () -> {
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.println(String.format(">> %sLong click on button 1", (buttonTwo.isPushed() ? "[Shft] + " : "")));
		}
		if (buttonTwo.isPushed()) { // Shift + LongClick on button one
			if (oledForwarder != null) {
				oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
				oledForwarder.displayLines(new String[] { "Shutting down...", "Confirm with",  "double-click (top)", "within 3 s"});
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
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.println(String.format(">> %sSingle click on button 2", (buttonOne.isPushed() ? "[Shft] + " : "")));
		}
		if (screenSaverMode) {
			releaseScreenSaver();
		} else if (displayingLocalMenu) {
			// Previous menu item
			localMenuItemIndex -= 1;
			if (localMenuItemIndex < 0) {
				localMenuItemIndex = (localMenuItems.length - 1);
			}
			displayLocalMenu();
		} else if (!buttonOne.isPushed() && oledForwarder != null) {
			if (buttonVerbose) {
				System.out.println("1 down!");
			}
			oledForwarder.onButtonDownPressed();
		}
	};

	private Runnable onDoubleClickTwo = () -> {
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.println(String.format(">> %sDouble click on button 2", (buttonOne.isPushed() ? "[Shft] + " : "")));
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
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.println(String.format(">> %sLong click on button 2", (buttonOne.isPushed() ? "[Shft] + " : "")));
		}
	};

	/**
	 *  Now using all the above
	 *  See {@link PushButtonController#isPushed()}
	 */
	public ServerWithKewlButtons() {

		super(); // NavServer

		this.turnLoggingOnURL = String.format("http://localhost:%d/mux/mux-process/on", serverPort);
		this.turnLoggingOffURL = String.format("http://localhost:%d/mux/mux-process/off", serverPort);
		this.terminateMuxURL = String.format("http://localhost:%d/mux/terminate", serverPort);

		System.out.println(String.format("To turn logging ON, user PUT %s", this.turnLoggingOnURL));
		System.out.println(String.format("To turn logging OFF, user PUT %s", this.turnLoggingOffURL));
		System.out.println(String.format("To terminate the multiplexer, user POST %s", this.terminateMuxURL));

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
			if (pair[1].startsWith("192.168.")) {
				sb.append(pair[1]);
			}
		});
		if (sb.length() > 0) {
			machineName = sb.toString();
		}
		System.out.println(String.format("Also try http://%s:%d/zip/index.html from a browser", machineName, serverPort));
		System.out.println(String.format("     and http://%s:%d/zip/runner.html ", machineName, serverPort));

		// Help display here
		System.out.println("+-----------------------------------------------------------------------------------------+");
		System.out.println("| Button-2 + LongClick on Button-1: Shutdown (confirm with double-click within 3 seconds) |");
		System.out.println("| DoubleClick on Button-1: Show local menu                                                |");
		System.out.println("| DoubleClick on Button-2: Screen Saver mode. Any simple-click to resume.                 |");
		System.out.println("+-----------------------------------------------------------------------------------------+");

		try {
			// Provision buttons here
			buttonOnePin = RaspiPin.GPIO_29; // Physical #38.
			buttonTwoPin = RaspiPin.GPIO_28; // Physical #40.

			// Change pins, based on system properties. Use physical pin numbers.
			try {
				// Identified by the PHYSICAL pin numbers
				String buttonOnePinStr = System.getProperty("buttonOne", "38"); // GPIO_28
				String buttonTwoPinStr = System.getProperty("buttonTwo", "40"); // GPIO_29

				buttonOnePin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonOnePinStr));
				buttonTwoPin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonTwoPinStr));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			buttonOne.update(
					"Top-Button",
					buttonOnePin,
					onClickOne,
					onDoubleClickOne,
					onLongClickOne);

			buttonTwo.update(
					"Bottom-Button",
					buttonTwoPin,
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
		if (oledForwarder == null) {
			System.out.println("SSD1306 was NOT loaded");
		} else {
			boolean simulating = oledForwarder.isSimulating();

			int SHIFT_KEY = 16,
					CTRL_KEY = 17;
			System.out.println(String.format("SSD1306 was loaded! (%s)", simulating ? "simulating" : "for real"));
			if (simulating) {
				oledForwarder.setSimutatorLedColor(Color.WHITE);
				System.out.println(">> Simulating button with [Shift]: Button 1: Ctrl, Button 2: Shift");
				oledForwarder.setSimulatorKeyPressedConsumer((keyEvent) -> {
//					System.out.println("KeyPressed:" + keyEvent);
					if (keyEvent.getKeyCode() == SHIFT_KEY) { // Shift, left or right
						buttonTwo.manageButtonState(PushButtonController.ButtonStatus.HIGH);
					} else if (keyEvent.getKeyCode() == CTRL_KEY) {
						buttonOne.manageButtonState(PushButtonController.ButtonStatus.HIGH);
					}
				});
				oledForwarder.setSimulatorKeyReleasedConsumer((keyEvent) -> {
//					System.out.println("KeyReleased:" + keyEvent);
					if (keyEvent.getKeyCode() == SHIFT_KEY) { // Shift, left or right
						buttonTwo.manageButtonState(PushButtonController.ButtonStatus.LOW);
					} else if (keyEvent.getKeyCode() == CTRL_KEY) {
						buttonOne.manageButtonState(PushButtonController.ButtonStatus.LOW);
					}
				});
			}
			// Following block just for tests and dev.
			if (false) {
				// Now let's write in the screen...
				TimeUtil.delay(10_000L);
				System.out.println("Taking ownership on the screen");
				oledForwarder.setExternallyOwned(true); // Taking ownership on the screen
				TimeUtil.delay(500L);
//			oledForwarder.displayLines(new String[] { "Taking ownership", "on the screen"});
				oledForwarder.displayLines(new String[]{"Shutting down...", "Confirm with", "double-click (top)", "within 3 s"});
				TimeUtil.delay(4_000L);

				oledForwarder.displayLines(new String[]{
						"Up and down to Scroll",
						"--------------------",
						"- Menu Operation", // Sample
						"--------------------",
						"Db-clk 1: select",
						"Db-clk 2: cancel"
				});
				TimeUtil.delay(5_000L);

				oledForwarder.displayLines(new String[]{"Releasing the screen"});
				TimeUtil.delay(2_000L);
				System.out.println("Releasing ownership on the screen");
				oledForwarder.setExternallyOwned(false); // Releasing ownership on the screen
			}
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
		buttonOne.freeResources();
		buttonTwo.freeResources();
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
