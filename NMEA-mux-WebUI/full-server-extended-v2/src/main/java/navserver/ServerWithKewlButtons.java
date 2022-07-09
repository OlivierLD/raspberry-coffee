package navserver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import http.client.HTTPClient;
import navrest.NavServer;
import utils.gpio.PushButtonController;
import nmea.api.Multiplexer;
import nmea.forwarders.SSD1306Processor;
import nmea.mux.GenericNMEAMultiplexer;
import utils.PinUtil;
import utils.StaticUtil;
import utils.SystemUtils;
import utils.TimeUtil;
import utils.gpio.StringToGPIOPin;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Shows how to add two push buttons to interact with the NavServer
 * Buttons with click, double-click, long-click, and other combinations.
 * Uses a small screen (oled SSD1306, Nokia, etc)
 *
 * This class use making use of Runnable and Consumers (thank you Java 8).
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

	// ----- Local Menu Operations, one Runnable for each operation -----
	private Runnable dummyOp = () -> {
		try {
			if (oledForwarder != null) {
				oledForwarder.displayLines(new String[]{ "PlaceHolder, for dev" });
				TimeUtil.delay(4_000L);
			}
		} catch (Exception ex) {
			System.err.println("Dummy Op:");
			ex.printStackTrace();
		}
	};

	private Runnable loggingStatus = () -> {
		try {
			String loggingStatus = HTTPClient.doGet(this.getLoggingStatusURL, new HashMap<>());
			/*
					{
					    "processing": true,
					    "started": 1570376199022
					}
			 */
			JsonObject json = new JsonParser().parse(loggingStatus).getAsJsonObject();
			boolean status = json.get("processing").getAsBoolean();
			if (oledForwarder != null) {
				oledForwarder.displayLines(new String[]{ String.format("Logging is %s.", (status ? "ON" : "OFF")) });
				TimeUtil.delay(4_000L);
			}
		} catch (Exception ex) {
			System.err.println("Logging Status:");
			ex.printStackTrace();
		}
	};

	private Runnable muxConfig = () -> {

		Multiplexer multiplexer = this.getMultiplexer();
		System.out.println(String.format("Mux is a %s", (multiplexer != null ? multiplexer.getClass().getName() : "null")));
		Properties muxProperties = (this.getMultiplexer() instanceof GenericNMEAMultiplexer) ?
				((GenericNMEAMultiplexer)this.getMultiplexer()).getMuxProperties() :
				null;
		List<String> config = new ArrayList<>();
		if (muxProperties == null) {
			// TODO http://localhost:port/mux/mux-config,
		} else {
//			System.out.println(muxProperties.stringPropertyNames()
//					.stream()
//					.map(prop -> String.format("%s=%s", prop, muxProperties.getProperty(prop)))
//					.collect(Collectors.joining(",\n")));
			muxProperties.stringPropertyNames().stream()
					.filter(prop -> prop.endsWith(".type") || prop.endsWith(".class"))
					.forEach(prop -> {
						String dir = prop.startsWith("mux") ? "IN" : "OUT";
						String data = prop.endsWith(".class") ?
								muxProperties.getProperty(prop).substring(muxProperties.getProperty(prop).lastIndexOf('.') + 1) :
						    muxProperties.getProperty(prop);
						config.add(String.format("%s %s", dir, data));
					});
		}

		try {
			if (oledForwarder != null) {
				oledForwarder.displayLines(config.toArray(new String[config.size()])); // TODO Scroll if needed
				TimeUtil.delay(4_000L);
			}
		} catch (Exception ex) {
			System.err.println("Dummy Op:");
			ex.printStackTrace();
		}
	};

	private Runnable getUserDir = () -> {
		try {
			String userDir = System.getProperty("user.dir");
			System.out.println(String.format("UserDir: %s", userDir));
			if (userDir.indexOf(File.separator) > -1) {
				userDir = "..." + userDir.substring(userDir.lastIndexOf(File.separatorChar));
			}
			if (oledForwarder != null) {
//				System.out.println(String.format("%s, len: %d", userDir, oledForwarder.strWidth(userDir)));
				int SCREEN_WIDTH = 128; // Hard-coded?
				String prefix = "...";
				if (oledForwarder.strWidth(userDir) > SCREEN_WIDTH) {
					while (userDir.length() > 0 && oledForwarder.strWidth(prefix + userDir) > (SCREEN_WIDTH - 1)) { // -1, nicer.
//						System.out.println(String.format("%s, len: %d", userDir, oledForwarder.strWidth(userDir)));
						userDir = userDir.substring(1);
					}
					userDir = prefix + userDir;
//					System.out.println(String.format("Finally %s, len: %d", userDir, oledForwarder.strWidth(userDir)));
				}
				oledForwarder.displayLines(new String[]{ String.format("Port %d", serverPort), "Running from", userDir });
				TimeUtil.delay(4_000L);
			}
		} catch (Exception ex) {
			System.err.println("Current Dir:");
			ex.printStackTrace();
		}
	};

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
			System.err.println("\t>> Forcing exit.");
			System.exit(1); // Force exit.
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

	private Runnable showImage = () -> {
		try {
			if (oledForwarder != null) {
				// Read ./img/image.dat
				try (DataInputStream imageStream = new DataInputStream(new FileInputStream("./img/image.dat"))) {
					int width = imageStream.readInt();
					int height = imageStream.readInt();
					if (width != 128 || height != 64) {
						System.out.println(String.format("Bad size %dx%d, expecting 128x64", width, height));
					} else {
						long[][] bitmap = new long[64][2];
						for (int line = 0; line < bitmap.length; line++) {
							bitmap[line][0] = imageStream.readLong();
							bitmap[line][1] = imageStream.readLong();
						}
						oledForwarder.displayBitmap(bitmap);
						TimeUtil.delay(5_000L);
					}
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		} catch (Exception ex) {
			System.err.println("Show image:");
			ex.printStackTrace();
		}
	};

	private Runnable displayNetworkParameters = () -> {
		List<String> display = new ArrayList<>();
		try {
			String command = "iwconfig"; // "iwconfig | grep wlan0 | awk '{ print $4 }'";
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			if (buttonVerbose) {
				System.out.println(String.format("Reading %s output", command));
			}
			while (line != null) {
				line = reader.readLine();
				if (line != null) {
					final String essid = "ESSID:";
					if (line.indexOf(essid) > -1) {
						display.add(line.substring(line.indexOf(essid) + essid.length()));
						if (buttonVerbose) {
							System.out.println(line);
						}
					}
				}
			}
			if (buttonVerbose) {
				System.out.println(String.format("Done with %s", command));
			}
			reader.close();
		} catch (Exception ex) {
			if (buttonVerbose) {
				ex.printStackTrace();
			}
		}
		// for demo...
		if (display.size() == 0) {
			display.add("Logger-Net");
		}

		try {
			String line = "IP Addr.:";
			display.add(line);
//		List<String> addresses = SystemUtils.getIPAddresses("wlan0", true);
			List<String[]> addresses = SystemUtils.getIPAddresses(true);
			for (String[] addr : addresses) {
//				line += (addr[1] + " ");
				display.add(addr[1]);
				if (buttonVerbose) {
					System.out.println(addr);
				}
			}
//			display.add(line);
		} catch (Exception ex) {
			if (buttonVerbose) {
				ex.printStackTrace();
			}
		}
		try {
			String hostName = SystemUtils.getHostName();
			if (hostName != null) {
				display.add(hostName);
			}
		} catch (Exception ex) {
			if (buttonVerbose) {
				ex.printStackTrace();
			}
		}
		if (oledForwarder != null) {
			oledForwarder.displayLines(display.toArray(new String[display.size()]));
			TimeUtil.delay(5_000L);
		}
	};

	private Runnable shutdown = () -> {
		try {
			StaticUtil.shutdown();
		} catch (Exception ex) {
			System.err.println("Shutdown:");
			ex.printStackTrace();
		}
	};

	private Runnable reboot = () -> {
		try {
			StaticUtil.reboot();
		} catch (Exception ex) {
			System.err.println("Reboot:");
			ex.printStackTrace();
		}
	};
  // ----- End of Local Menu Operations -----

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

	private MenuItem[] localMenuItems = new MenuItem[] {
			new MenuItem().title("Logging status").action(loggingStatus),
			new MenuItem().title("Pause logging").action(pauseLogging),
			new MenuItem().title("Resume logging").action(resumeLogging),
			new MenuItem().title("Terminate Multiplexer").action(terminateMux),
			new MenuItem().title("-> Shutdown !").action(shutdown),
			new MenuItem().title("-> Reboot !").action(reboot),
			new MenuItem().title("Network Config").action(displayNetworkParameters),
			new MenuItem().title("Mux Config").action(muxConfig),
			new MenuItem().title("Running from").action(getUserDir),
			new MenuItem().title("Say Hello").action(sayHello),                      // As an example...
			new MenuItem().title("Show image").action(showImage)                     // Example too
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
	private String getLoggingStatusURL = "";

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
			oledForwarder.displayLines(
					"> Up and down to Scroll",
					"--------------------",
					localMenuItems[localMenuItemIndex].getTitle(),
					"--------------------",
					"> Db-clk 1: select",
					"> Db-clk 2: cancel");
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

	/* ----- Buttons Runnables (actions) ----- */
	private Runnable onClickOne = () -> {
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
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
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
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
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
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
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
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
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
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
			startScreenSaver();
		} else {
			System.out.println("Already in screensaver mode");
		}
	};

	private Runnable onLongClickTwo = () -> {
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.println(String.format(">> %sLong click on button 2", (buttonOne.isPushed() ? "[Shft] + " : "")));
		}
	};
	/* ---- End of Buttons Runnables ---- */

	private void startScreenSaver() {
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
	}

	private long lastButtonInteraction = 0L;
	private final static long GO_TO_SLEEP_AFTER = 5 * 60_000L; // 5 minutes
	/**
	 *  Now using all the above.
	 *  Notice the call made to {@link SSD1306Processor#setSimulatorKeyPressedConsumer(Consumer)}
	 *  and {@link SSD1306Processor#setSimulatorKeyReleasedConsumer(Consumer)}
	 *  They implement the buttons simulators.
	 */
	public ServerWithKewlButtons() {

		super(); // NavServer

		Multiplexer multiplexer = this.getMultiplexer();
		System.out.println(String.format("Mux is a %s", (multiplexer != null ? multiplexer.getClass().getName() : "null")));
		Properties muxProperties = (this.getMultiplexer() instanceof GenericNMEAMultiplexer) ?
				((GenericNMEAMultiplexer)this.getMultiplexer()).getMuxProperties() :
				null;

		try {
			serverPort = Integer.parseInt(System.getProperty("http.port", String.valueOf(serverPort)));
		} catch (NumberFormatException nfe) {
			System.err.println("Ooops");
			nfe.printStackTrace();
		}
		System.out.println(String.format(">>> Server port is %d", serverPort));


		System.out.println(">> Starting extension (after super())...");

		this.turnLoggingOnURL = String.format("http://localhost:%d/mux/mux-process/on", serverPort);
		this.turnLoggingOffURL = String.format("http://localhost:%d/mux/mux-process/off", serverPort);
		this.terminateMuxURL = String.format("http://localhost:%d/mux/terminate", serverPort);
		this.getLoggingStatusURL = String.format("http://localhost:%d/mux/mux-process", serverPort);

		System.out.println(String.format("To turn logging ON, use PUT %s", this.turnLoggingOnURL));
		System.out.println(String.format("To turn logging OFF, use PUT %s", this.turnLoggingOffURL));
		System.out.println(String.format("To terminate the multiplexer, use POST %s", this.terminateMuxURL));

		List<String[]> addresses = SystemUtils.getIPAddresses(true);
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
			buttonOnePin = RaspiPin.GPIO_28; // Physical #38.
			buttonTwoPin = RaspiPin.GPIO_29; // Physical #40.

			// Change pins, based on system properties.
			// Use physical pin numbers.
			try {
				// Identified by the PHYSICAL pin numbers
				String buttonOnePinStr = System.getProperty("buttonOne", String.valueOf(PinUtil.getPhysicalByWiringPiNumber(buttonOnePin.getName()))); // GPIO_28
				String buttonTwoPinStr = System.getProperty("buttonTwo", String.valueOf(PinUtil.getPhysicalByWiringPiNumber(buttonTwoPin.getName()))); // GPIO_29

				buttonOnePin = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonOnePinStr)));
				buttonTwoPin = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonTwoPinStr)));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			// Pin mapping display for info
			String[] map = new String[13];
			map[0]  = String.valueOf(PinUtil.findByPin(buttonOnePin.getName()).pinNumber()) + ":Button 1 Hot Wire";
			map[1]  = String.valueOf(PinUtil.findByPin(buttonTwoPin.getName()).pinNumber()) + ":Button 2 Hot Wire";

			map[2]  = String.valueOf(PinUtil.GPIOPin.PWR_1.pinNumber())   + ":3v3";
			map[3]  = String.valueOf(PinUtil.GPIOPin.PWR_2.pinNumber())   + ":5v0";

			map[4]  = String.valueOf(PinUtil.GPIOPin.GPIO_15.pinNumber()) + ":Tx";
			map[5]  = String.valueOf(PinUtil.GPIOPin.GPIO_16.pinNumber()) + ":Rx";

			map[6]  = String.valueOf(PinUtil.GPIOPin.GPIO_14.pinNumber()) + ":Clock";
			map[7]  = String.valueOf(PinUtil.GPIOPin.GPIO_12.pinNumber()) + ":Data"; // Aka MOSI. Slave is the Screen, Master the RPi
			map[8]  = String.valueOf(PinUtil.GPIOPin.GPIO_10.pinNumber()) + ":CS";
			map[9]  = String.valueOf(PinUtil.GPIOPin.GPIO_5.pinNumber())  + ":Rst";
			map[10] = String.valueOf(PinUtil.GPIOPin.GPIO_4.pinNumber())  + ":DC";

			map[11] = String.valueOf(PinUtil.GPIOPin.GPIO_8.pinNumber())  + ":SDA";
			map[12] = String.valueOf(PinUtil.GPIOPin.GPIO_9.pinNumber())  + ":SLC";

			System.out.println("---------------------------- P I N   M A P P I N G ------------------------------------------");
			PinUtil.print(map);
			System.out.println("> Buttons, Screen and GPS are powered with 5.0, 3v3 is used by the BME280.");
			System.out.println("---------------------------------------------------------------------------------------------\n");

			// Button-1 provisioning, with its operations
			buttonOne.update(
					"Top-Button",
					buttonOnePin,
					onClickOne,
					onDoubleClickOne,
					onLongClickOne);

			// Button-2 provisioning, with its operations
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
		oledForwarder = SSD1306Processor.getInstance(); // A singleton.
		if (oledForwarder == null) {
			System.out.println("SSD1306 was NOT loaded");
		} else {
			boolean simulating = oledForwarder.isSimulating();

			oledForwarder.setSimulatorTitle("Simulating SSD1306 - Button 1: Ctrl, Button 2: Shift");

			final int SHFT_KEY = 16,
								CTRL_KEY = 17;

			System.out.println(String.format("SSD1306 was loaded! (%s)", simulating ? "simulating" : "for real"));
			if (simulating) {
				// Simulator led color
				oledForwarder.setSimutatorLedColor(Color.WHITE);
				// Seems not to be possible to have left shift and right shift. When one is on, the other is ignored.
				// Buttons simulator
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				System.out.println(">> Simulating buttons: Button 1: Ctrl, Button 2: Shift");
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				// Runnables for the simulator
				oledForwarder.setSimulatorKeyPressedConsumer((keyEvent) -> {
//					System.out.println("KeyPressed:" + keyEvent);
					if (keyEvent.getKeyCode() == SHFT_KEY) { // Shift, left or right
						buttonTwo.manageButtonState(PushButtonController.ButtonStatus.HIGH);
					} else if (keyEvent.getKeyCode() == CTRL_KEY) {
						buttonOne.manageButtonState(PushButtonController.ButtonStatus.HIGH);
					}
				});
				oledForwarder.setSimulatorKeyReleasedConsumer((keyEvent) -> {
//					System.out.println("KeyReleased:" + keyEvent);
					if (keyEvent.getKeyCode() == SHFT_KEY) { // Shift, left or right
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
				oledForwarder.displayLines(new String[]{
						"Shutting down...",
						"Confirm with",
						"double-click (top)",
						"within 3 s"});
				TimeUtil.delay(4_000L);

				oledForwarder.displayLines(
						"Up and down to Scroll",
						"--------------------",
						"- Menu Operation",        // <- Sample
						"--------------------",
						"Db-clk 1: select",
						"Db-clk 2: cancel");
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

		lastButtonInteraction = System.currentTimeMillis(); // used to know when to start the screen saver
		Thread sleeper = new Thread(() -> {
			while (true) {
				if (!screenSaverMode && (System.currentTimeMillis() - lastButtonInteraction) > GO_TO_SLEEP_AFTER) {
					startScreenSaver();
				}
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}, "sleeper");
		sleeper.start();
	}

	public static void freeResources() {
		// Cleanup
		buttonOne.freeResources();
		buttonTwo.freeResources();
	}

	public static void main(String... args) {

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			freeResources();
		}, "Shutdown Hook"));
		NavServer server = new ServerWithKewlButtons();

	}

}
