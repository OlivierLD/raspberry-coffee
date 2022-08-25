package mux;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import http.client.HTTPClient;
import nmea.forwarders.SSD1306Processor;
import nmea.mux.GenericNMEAMultiplexer;
import utils.*;
import utils.gpio.PushButtonController;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Shows how to add two push buttons to interact with the NavServer
 * Buttons with click, double-click, long-click, and other combinations.
 * Uses a small screen (oled SSD1306, Nokia, etc)
 *
 * This class is using Runnables and Consumers (thank you Java 8).
 *
 * System properties:
 * - button.verbose, default false
 * - buttonOne, default 38 (Physical pin #)
 * - buttonTwo, default 40 (Physical pin #)
 * - http.port, default 9999
 *
 * If simulating (no physical devices, screen or buttons), buttons can be simulated too,
 * - Button 1: Ctrl, Button 2: Shift
 */
public class MultiplexerWithTwoButtons extends GenericNMEAMultiplexer {

	private final static boolean buttonVerbose = "true".equals(System.getProperty("button.verbose"));

	private SSD1306Processor oledForwarder;

	// ----- Local Menu Operations, one Runnable for each operation -----
	private final Runnable muxConfig = () -> {

		Properties muxProperties = this.getMuxProperties();
		List<String> config = new ArrayList<>();
		if (muxProperties == null) {
			// TODO http://localhost:port/mux/mux-config,
			System.err.println("No muxProperties !!");
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
			System.err.println("MuxConfig:");
			ex.printStackTrace();
		}
	};

	private final Runnable getUserDir = () -> {
		try {
			String userDir = System.getProperty("user.dir");
			System.out.printf("UserDir: %s\n", userDir);
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

	private final Runnable loggingStatus = () -> {
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

	private final Runnable pauseLogging = () -> {
		try {
			HTTPClient.doPut(this.turnLoggingOffURL, new HashMap<>(), null);
		} catch (Exception ex) {
			System.err.println("Pausing logging:");
			ex.printStackTrace();
		}
	};

	private final Runnable resumeLogging = () -> {
		try {
			HTTPClient.doPut(this.turnLoggingOnURL, new HashMap<>(), null);
		} catch (Exception ex) {
			System.err.println("Resuming logging:");
			ex.printStackTrace();
		}
	};

	private final Runnable terminateMux = () -> {
		try {
			HTTPClient.doPost(this.terminateMuxURL, new HashMap<>(), null);
		} catch (Exception ex) {
			System.err.println("Terminate Mux:");
			ex.printStackTrace();
			System.err.println("\t>> Forcing exit.");
			System.exit(1); // Force exit.
		}
	};

	private final Runnable sayHello = () -> {
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

	private final Runnable showImage = () -> {
		try {
			if (oledForwarder != null) {
				// Read ./img/image.dat
				try (DataInputStream imageStream = new DataInputStream(new FileInputStream("./img/image.dat"))) {
					int width = imageStream.readInt();
					int height = imageStream.readInt();
					if (width != 128 || height != 64) {
						System.out.printf("Bad size %dx%d, expecting 128x64\n", width, height);
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

	private final Runnable displayNetworkParameters = () -> {
		List<String> display = new ArrayList<>();
		try {
			String command = "iwconfig"; // "iwconfig | grep wlan0 | awk '{ print $4 }'";
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			if (buttonVerbose) {
				System.out.printf("Reading %s output\n", command);
			}
			while (line != null) {
				line = reader.readLine();
				if (line != null) {
					final String essid = "ESSID:";
					if (line.contains(essid)) {
						display.add(line.substring(line.indexOf(essid) + essid.length()));
						if (buttonVerbose) {
							System.out.println(line);
						}
					}
				}
			}
			if (buttonVerbose) {
				System.out.printf("Done with %s\n", command);
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

	private final Runnable shutdown = () -> {
		try {
			StaticUtil.shutdown();
		} catch (Exception ex) {
			System.err.println("Shutdown:");
			ex.printStackTrace();
		}
	};

	private final Runnable reboot = () -> {
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
	private static class MenuItem {
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

	private final MenuItem[] localMenuItems = new MenuItem[] {
  			new MenuItem().title("Logging status").action(loggingStatus),
			new MenuItem().title("Pause logging").action(pauseLogging),
			new MenuItem().title("Resume logging").action(resumeLogging),
			new MenuItem().title("Terminate Multiplexer").action(terminateMux),
			new MenuItem().title("-> Shutdown !").action(shutdown),
			new MenuItem().title("-> Reboot !").action(reboot),
			new MenuItem().title("Network Config").action(displayNetworkParameters),
			new MenuItem().title("Mux Config").action(muxConfig),
			new MenuItem().title("Running from").action(getUserDir),
			new MenuItem().title("Say Hello").action(sayHello),                       // As an example...
			new MenuItem().title("Show image").action(showImage)                      // As an example...
	};
	private int localMenuItemIndex = 0;

	private Pin buttonOnePin; // Top
	private Pin buttonTwoPin; // Bottom

	final static PushButtonController buttonOne = new PushButtonController();
	final static PushButtonController buttonTwo = new PushButtonController();

	private static int serverPort = 9999;
	private String getLoggingStatusURL = "";
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
			oledForwarder.displayLines(
					"> Up and down to Scroll",
					"--------------------",
					localMenuItems[localMenuItemIndex].getTitle(),
					"--------------------",
					"> Db-clk 1: select",
					"> Db-clk 2: cancel");
		}
	}

	private final static List<String> HELP_CONTENT = Arrays.asList(  // Java 8 does not like List.of...
			"Button-2 + LongClick on Button-1: Shutdown (confirm with double-click within 3 seconds)",
			"DoubleClick on Button-1: Show local menu",
			"DoubleClick on Button-2: Screen Saver mode. Any simple-click to resume."
	);

	private void displayHelp() {
		final String HEADER = "U S A G E";
		int maxLineLength = HELP_CONTENT.stream().map(line -> line.length()).max(Comparator.comparing(i -> i)).get();
		System.out.println("+-" + StringUtils.rpad("-", maxLineLength, "-") + "-+" );
		System.out.println("| " + StringUtils.rpad(StringUtils.lpad("", ((maxLineLength - HEADER.length()) / 2)) + HEADER, maxLineLength) + " |");
		System.out.println("+-" + StringUtils.rpad("-", maxLineLength, "-") + "-+" );
		HELP_CONTENT.forEach(line -> {
			System.out.println("| " + StringUtils.rpad(line, maxLineLength) + " |");
		});
		System.out.println("+-" + StringUtils.rpad("-", maxLineLength, "-") + "-+" );
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
	private final Runnable onClickOne = () -> {
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.printf(">> %sSingle click on button 1\n", (buttonTwo.isPushed() ? "[Shft] + " : ""));
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

	private final Runnable onDoubleClickOne = () -> {
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.printf(">> %sDouble click on button 1\n", (buttonTwo.isPushed() ? "[Shft] + " : ""));
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

	private final Runnable onLongClickOne = () -> {
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.printf(">> %sLong click on button 1\n", (buttonTwo.isPushed() ? "[Shft] + " : ""));
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

	private final Runnable onClickTwo = () -> {
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.printf(">> %sSingle click on button 2\n", (buttonOne.isPushed() ? "[Shft] + " : ""));
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

	private final Runnable onDoubleClickTwo = () -> {
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.printf(">> %sDouble click on button 2\n", (buttonOne.isPushed() ? "[Shft] + " : ""));
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

	private final Runnable onLongClickTwo = () -> {
		// Timestamp. Go to screen saver mode after a given threshold
		lastButtonInteraction = System.currentTimeMillis();
		if (buttonVerbose || oledForwarder.isSimulating()) {
			System.out.printf(">> %sLong click on button 2\n", (buttonOne.isPushed() ? "[Shft] + " : ""));
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
	public MultiplexerWithTwoButtons(Properties muxProps) {

		super(muxProps); // GenericNMEAMultiplexer
		System.out.println(">> Starting extension (after super())...");

		this.getLoggingStatusURL = String.format("http://localhost:%d/mux/mux-process", serverPort);
		this.turnLoggingOnURL = String.format("http://localhost:%d/mux/mux-process/on", serverPort);
		this.turnLoggingOffURL = String.format("http://localhost:%d/mux/mux-process/off", serverPort);
		this.terminateMuxURL = String.format("http://localhost:%d/mux/terminate", serverPort);

		System.out.printf("To turn logging ON, use PUT %s\n", this.turnLoggingOnURL);
		System.out.printf("To turn logging OFF, use PUT %s\n", this.turnLoggingOffURL);
		System.out.printf("To terminate the multiplexer, use POST %s\n", this.terminateMuxURL);

		System.out.printf("\nREST Operations: GET http://localhost:%d/mux/oplist\n\n", serverPort);

		List<String[]> addresses = SystemUtils.getIPAddresses(true);
		String machineName = "localhost";
		if (addresses.size() == 1) {
			machineName = addresses.get(0)[1];
		}
		StringBuffer sb = new StringBuffer();
		System.out.println("IP addresses for localhost:");
		addresses.forEach(pair -> {
			System.out.printf("%s -> %s\n", pair[0], pair[1]);
			// for tests
			if (pair[1].startsWith("192.168.")) { // ...a bit tough. I know.
				sb.append(pair[1]);
			}
		});
		if (sb.length() > 0) {
			machineName = sb.toString();
		}
		System.out.printf("Also try http://%s:%d/web/index.html from a browser\n", machineName, serverPort);

		// Help display here
		displayHelp();

		try {
			// Provision buttons here
			buttonOnePin = RaspiPin.GPIO_28; // Physical #38.
			buttonTwoPin = RaspiPin.GPIO_29; // Physical #40.

			// Change pins, based on system properties.
			// Use physical pin numbers.
			try {
				// Identified by the PHYSICAL pin numbers
				String buttonOnePinStr = System.getProperty("buttonOne", String.valueOf(PinUtil.getPhysicalByWiringPiNumber(buttonOnePin))); // GPIO_28
				String buttonTwoPinStr = System.getProperty("buttonTwo", String.valueOf(PinUtil.getPhysicalByWiringPiNumber(buttonTwoPin))); // GPIO_29

				buttonOnePin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonOnePinStr));
				buttonTwoPin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonTwoPinStr));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			// Pin mapping display for info
			List<String> pinList = new ArrayList<>();
			pinList.add((PinUtil.findByPin(buttonOnePin).pinNumber()) + ":Button 1 Hot Wire");
			pinList.add((PinUtil.findByPin(buttonTwoPin).pinNumber()) + ":Button 2 Hot Wire");

			pinList.add((PinUtil.GPIOPin.PWR_1.pinNumber())   + ":3v3");
			pinList.add((PinUtil.GPIOPin.PWR_2.pinNumber())   + ":5v0");

			pinList.add((PinUtil.GPIOPin.GPIO_15.pinNumber()) + ":Tx");
			pinList.add((PinUtil.GPIOPin.GPIO_16.pinNumber()) + ":Rx");

			pinList.add((PinUtil.GPIOPin.GPIO_14.pinNumber()) + ":Clock");
			pinList.add((PinUtil.GPIOPin.GPIO_12.pinNumber()) + ":Data"); // Aka MOSI. Slave is the Screen, Master the RPi
			pinList.add((PinUtil.GPIOPin.GPIO_10.pinNumber()) + ":CS");
			pinList.add((PinUtil.GPIOPin.GPIO_5.pinNumber())  + ":Rst");
			pinList.add((PinUtil.GPIOPin.GPIO_4.pinNumber())  + ":DC");

			pinList.add((PinUtil.GPIOPin.GPIO_8.pinNumber())  + ":SDA");
			pinList.add((PinUtil.GPIOPin.GPIO_9.pinNumber())  + ":SLC");

			System.out.println("---------------------------- P I N   M A P P I N G ------------------------------------------");
			PinUtil.print(pinList.toArray(new String[pinList.size()]));
			System.out.println("> Buttons, Screen and GPS are powered with 5.0, 3v3 is used by the BME280.");
			System.out.println("---------------------------------------------------------------------------------------------\n");

			// Button-1 provisioning, with its name and operations
			buttonOne.update(
					"Top-Button",
					buttonOnePin,
					onClickOne,
					onDoubleClickOne,
					onLongClickOne);

			// Button-2 provisioning, with its name and operations
			buttonTwo.update(
					"Bottom-Button",
					buttonTwoPin,
					onClickTwo,
					onDoubleClickTwo,
					onLongClickTwo);

		} catch (Throwable error) {
			error.printStackTrace();
		}

		System.out.println(">> Buttons provisioned.");

		// Was the SSD1306 loaded? This is loaded by the properties file.
		// Use the SSD1306Processor, SPI version.
		oledForwarder = SSD1306Processor.getInstance(); // A singleton.
		if (oledForwarder == null) {
			System.out.println("SSD1306 was NOT loaded");
		} else {
			boolean simulating = oledForwarder.isSimulating();

			final int SHFT_KEY = 16,
					  CTRL_KEY = 17,
			          H_KEY    = 72;

			System.out.printf("SSD1306 was loaded! (%s)\n", simulating ? "simulating" : "for real");
			if (simulating) { // Add buttons, to simulate clicks.
				oledForwarder.setSimulatorTitle("Simulating SSD1306 - Button 1: Ctrl, Button 2: Shift, H: Help"); // For user's info.
				// Simulator led color
				oledForwarder.setSimutatorLedColor(Color.WHITE);
				// Seems not to be possible to have left shift and right shift. When one is on, the other is ignored.
				// Buttons simulator
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				System.out.println(">> Simulating buttons: Button 1: Ctrl, Button 2: Shift, H: Help");
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
				// Runnables for the simulator
				// Buttons pushed:
				oledForwarder.setSimulatorKeyPressedConsumer((keyEvent) -> {
					if (buttonVerbose) {
						System.out.println("KeyPressed:" + keyEvent);
					}
					if (keyEvent.getKeyCode() == SHFT_KEY) { // Shift, left or right
						buttonTwo.manageButtonState(PushButtonController.ButtonStatus.HIGH);
					} else if (keyEvent.getKeyCode() == CTRL_KEY) {
						buttonOne.manageButtonState(PushButtonController.ButtonStatus.HIGH);
					} else if (keyEvent.getKeyCode() == H_KEY) {
//						displayHelp(); // raw output, console.
						JFrame substitute = oledForwarder.getSwingLedPanel();
						JOptionPane.showMessageDialog(substitute,
								HELP_CONTENT.stream().collect(Collectors.joining("\n")),
								"Help!",
								JOptionPane.PLAIN_MESSAGE);
					}
				});
				// Buttons released:
				oledForwarder.setSimulatorKeyReleasedConsumer((keyEvent) -> {
					if (buttonVerbose) {
						System.out.println("KeyReleased:" + keyEvent);
					}
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
		System.out.println(">> Nav Server fully initialized");

		lastButtonInteraction = System.currentTimeMillis(); // used to know when to start the screen saver
		// This one starts the screen saver after a given time of inactivity
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

		boolean infraVerbose = "true".equals(System.getProperty("mux.infra.verbose"));

		try {
			serverPort = Integer.parseInt(System.getProperty("http.port", String.valueOf(serverPort)));
		} catch (NumberFormatException nfe) {
			System.err.println("Ooops");
			nfe.printStackTrace();
		}
		System.out.printf(">>> Server port is %d\n", serverPort);

		Runtime.getRuntime().addShutdownHook(new Thread(MultiplexerWithTwoButtons::freeResources, "Shutdown Hook"));
		Properties definitions = GenericNMEAMultiplexer.getDefinitions();

		boolean startProcessingOnStart = "true".equals(System.getProperty("process.on.start", "true"));
		MultiplexerWithTwoButtons mux = new MultiplexerWithTwoButtons(definitions);
		mux.setEnableProcess(startProcessingOnStart);
		// with.http.server=yes
		// http.port=9999
		String withHttpServer = definitions.getProperty("with.http.server", "no");
		if ("yes".equals(withHttpServer) || "true".equals(withHttpServer)) {
			mux.startAdminServer(Integer.parseInt(definitions.getProperty("http.port", "9999")));
		} else {
			if (infraVerbose) {
				System.out.println(">> NO ADMIN Server started!!");
			}
		}
	}

}
