package main;

import com.pi4j.io.gpio.PinState;
import org.fusesource.jansi.AnsiConsole;
import relay.RelayDriver;
import sensors.sth10.STH10Driver;
import utils.PinUtil;
import utils.StaticUtil;
import utils.WeatherUtil;

import java.util.Arrays;
import java.util.function.Supplier;
import static utils.StringUtils.rpad;
import static utils.TimeUtil.fmtDHMS;
import static utils.TimeUtil.msToHMS;

import static main.EscapeSeq.*;

/**
 * Example / Prototype...
 */
public class STH10 {

	private static boolean go = true;

	private final static int HUMIDITY_THRESHOLD = 35; // 35 %
	private final static long WATERING_DURATION = 10L; // 10 seconds
	private final static long RESUME_SENSOR_WATCH_AFTER = 120L; // 2 minutes

	// Default values
	private static int humidityThreshold = HUMIDITY_THRESHOLD;
	private static long wateringDuration = WATERING_DURATION;
	private static long resumeSensorWatchAfter = RESUME_SENSOR_WATCH_AFTER;

	// Program arguments
	private enum ARGUMENTS {
		HUMIDITY_THRESHOLD("--water-below:", // %
				"Integer. Humidity threshold in %, default is --water-below:35, start watering below this value."),
		WATERING_DURATION("--water-during:", // seconds
				"Integer. In seconds, default is --water-during:10. Duration of the watering process."),
		RESUME_AFTER("--resume-after:", // seconds
				"Integer. In seconds, default is --resume-after:120. After watering, resume sensor monitoring after this amount of time."),
		VERBOSE("--verbose:", // true|false
				"String. Verbose, default is --verbose:NONE, values can be 'NONE', 'STDOUT' or 'ANSI'."),
		DATA_PIN("--data-pin:", // default is BCM 18 => GPIO_01
				"Integer. BCM (aka GPIO) pin number of the DATA pin of the sensor. Default is --data-pin:18."),
		CLOCK_PIN("--clock-pin:", // default is BCM 23 => GPIO_04
				"Integer. BCM (aka GPIO) pin number of the CLOCK pin of the sensor. Default is --clock-pin:23."),
		RELAY_PIN("--relay-pin:",  // default is BCM 17 => GPIO_00
				"Integer. BCM (aka GPIO) pin number of the SIGNAL pin of the RELAY. Default is --relay-pin:17."),
		HELP("--help", "Display the help and exit.");

		private String prefix, help;

		ARGUMENTS(String prefix, String help) {
			this.prefix = prefix;
			this.help = help;
		}

		public String prefix() {
			return this.prefix;
		}
		public String help() {
			return this.help;
		}
	}

	enum VERBOSE {
		NONE,
		STDOUT,
		ANSI
	}
	private static VERBOSE verbose = VERBOSE.NONE;
	private final static String PAD = ANSI_ERASE_TO_EOL;

	private static STH10Driver probe = null;
	private static RelayDriver relay = null;

	// Simulators, to run on non-Raspberry PIs - for development.
//	private static Supplier<Double> temperatureSimulator = STH10::simulateUserTemp;
//	private static Supplier<Double> humiditySimulator = STH10::simulateUserHum;
	private static Supplier<Double> temperatureSimulator = STH10::simulateTemp;
	private static Supplier<Double> humiditySimulator = STH10::simulateHum;

	private static double temperature = 20d;
	private static double humidity = 50d;
	private static String message = "";

	private static double minSimTemp = temperature, maxSimTemp = temperature;
	private static double minSimHum = humidity, maxSimHum = humidity;

	private static Double simulateTemp() {
		int sign = (int)System.currentTimeMillis() % 2;
		double diff = Math.random() * (sign == 0 ? 1 : -1);
		temperature += diff;
		minSimTemp = Math.min(minSimTemp, temperature);
		maxSimTemp = Math.max(maxSimTemp, temperature);
		return temperature;
	}
	private static Double simulateHum() {
		int sign = (int)System.currentTimeMillis() % 2;
		double diff = Math.random() * (sign == 0 ? 1 : -1);
		humidity += diff;
		minSimHum = Math.min(minSimHum, humidity);
		maxSimHum = Math.max(maxSimHum, humidity);
		return humidity;
	}

	// Interactive simulators, for dev and tests.
	private static Double simulateUserTemp() {
		return temperature;
	}
	private static Double simulateUserHum() {
		return humidity;
	}

	private static void parseUserInput(String str) {
		// Input can be T:XX or H:xx
		if (str.startsWith("T:")) {
			try {
				temperature = Double.parseDouble(str.substring("T:".length()));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		} else if (str.startsWith("H:")) {
			try {
				humidity = Double.parseDouble(str.substring("H:".length()));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
	}

	/**	/**
	 * Returns a string of nb times the str parameter.
	 * @param str the string to use
	 * @param nb number of times
	 * @return the expected string.
	 */
	private static String drawXChar(String str, int nb) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<nb; i++) {
			sb.append(str);
		}
		return sb.toString();
	}

	private final static int FRAME_WIDTH = 50;
  /**
	 * Box codes are available at https://en.wikipedia.org/wiki/Box-drawing_character
	 * Display the data in an ANSI box, refreshed every time is is displayed.
	 */
	private static void displayAnsiData() {
		AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
		int line = 1; // Start from that line
		// Frame top
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				TOP_LEFT_CORNER_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				TOP_RIGHT_CORNER_BOLD +
				PAD);
		// Title. Note: The italic escape code is correct. But it does not work on all platforms.
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD  + ANSI_BOLD + ANSI_ITALIC + rpad("              PLANT WATERING SYSTEM ", FRAME_WIDTH) + ANSI_NORMAL + SOLID_VERTICAL_BOLD + PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// Program parameters
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Start watering under %d%% of humidity.", humidityThreshold), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Water during %s", fmtDHMS(msToHMS(wateringDuration * 1_000))), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Resume sensor watch %s after watering.", fmtDHMS(msToHMS(resumeSensorWatchAfter * 1_000))), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// Sensor Data
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Temp: %.02f C, Hum: %.02f%%", temperature, humidity), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// Message
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" %s", message), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Frame bottom
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				BOTTOM_LEFT_CORNER_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				BOTTOM_RIGHT_CORNER_BOLD +
				PAD);
	}

	public static void main(String... args) {

		int dataPin = 18, clockPin = 23, relayPin = 17; // Defaults

		// Override values with runtime arguments
		for (String arg : args) {
			if (arg.startsWith(ARGUMENTS.HELP.prefix())) {
				// No value, display help
				System.out.println("+---------------------------------------");
				System.out.println("| Program arguments are:");
				System.out.println("+---------------------------------------");
				Arrays.stream(ARGUMENTS.values()).forEach(argument -> System.out.println("| " +argument.prefix() + "\t" + argument.help()));
				System.out.println("+---------------------------------------");
				System.exit(0);
			} else if (arg.startsWith(ARGUMENTS.VERBOSE.prefix())) {
				String val = arg.substring(ARGUMENTS.VERBOSE.prefix().length());
				verbose = VERBOSE.valueOf(val);
			} else if (arg.startsWith(ARGUMENTS.DATA_PIN.prefix())) {
				String val = arg.substring(ARGUMENTS.DATA_PIN.prefix().length());
				try {
					dataPin = Integer.parseInt(val);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(ARGUMENTS.CLOCK_PIN.prefix())) {
				String val = arg.substring(ARGUMENTS.CLOCK_PIN.prefix().length());
				try {
					clockPin = Integer.parseInt(val);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(ARGUMENTS.RELAY_PIN.prefix())) {
				String val = arg.substring(ARGUMENTS.RELAY_PIN.prefix().length());
				try {
					relayPin = Integer.parseInt(val);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(ARGUMENTS.HUMIDITY_THRESHOLD.prefix())) {
				String val = arg.substring(ARGUMENTS.HUMIDITY_THRESHOLD.prefix().length());
				try {
					humidityThreshold = Integer.parseInt(val);
					if (humidityThreshold < 0 || humidityThreshold > 100) {
						humidityThreshold = HUMIDITY_THRESHOLD;
						System.err.println(String.format(">> Humidity Threshold must be in [0..100]. Reseting to %d ", HUMIDITY_THRESHOLD));
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(ARGUMENTS.WATERING_DURATION.prefix())) {
				String val = arg.substring(ARGUMENTS.WATERING_DURATION.prefix().length());
				try {
					wateringDuration = Long.parseLong(val);
					if (wateringDuration < 0) {
						wateringDuration = WATERING_DURATION;
						System.err.println(">> Watering duration must be positive. Ignoring.");
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(ARGUMENTS.RESUME_AFTER.prefix())) {
				String val = arg.substring(ARGUMENTS.RESUME_AFTER.prefix().length());
				try {
					resumeSensorWatchAfter = Long.parseLong(val);
					if (resumeSensorWatchAfter < 0) {
						resumeSensorWatchAfter = RESUME_SENSOR_WATCH_AFTER;
						System.err.println(">> Resume Watch After must be positive. Ignoring.");
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
		if (verbose == VERBOSE.ANSI) {
			AnsiConsole.systemInstall();
			AnsiConsole.out.println(EscapeSeq.ANSI_CLS);
		}
		// Print summary
		if (verbose == VERBOSE.ANSI) {
			displayAnsiData();
		} else {
			System.out.println("+------- P L A N T   W A T E R I N G   S Y S T E M --------");
			System.out.println(String.format("| Start watering under %d%% of humidity.", humidityThreshold));
			System.out.println(String.format("| Water during %s", fmtDHMS(msToHMS(wateringDuration * 1_000))));
			System.out.println(String.format("| Resume sensor watch %s after watering.", fmtDHMS(msToHMS(resumeSensorWatchAfter * 1_000))));
			System.out.println("+----------------------------------------------------------");
		}

		if (verbose == VERBOSE.STDOUT) {
			System.out.println("Wiring:");
			// Compose mapping for PinUtil, physical numbers.
			String[] map = new String[3];
			map[0] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByGPIONumber(dataPin)).pinNumber()) + ":" + "DATA";
			map[1] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByGPIONumber(clockPin)).pinNumber()) + ":" + "CLOCK";
			map[2] = String.valueOf(PinUtil.findByPin(PinUtil.getPinByGPIONumber(relayPin)).pinNumber()) + ":" + "RELAY";
			PinUtil.print(map);
		}

		try {
			probe = new STH10Driver(PinUtil.getPinByGPIONumber(dataPin), PinUtil.getPinByGPIONumber(clockPin));
			if (probe.isSimulating()) {
				// Provide simulator here
				System.out.println(">> Will simulate STH10");
				probe.setSimulators(temperatureSimulator, humiditySimulator);
			}
	  } catch (UnsatisfiedLinkError ule) { // That one is trapped in the constructor of STH10Driver.
			System.out.println("You're not on a Raspberry PI, or your wiring is wrong.");
			System.out.println("Exiting.");
			System.exit(1);
		}
		try {
			relay = new RelayDriver(PinUtil.getPinByGPIONumber(relayPin));
			if (relay.isSimulating()) {
				// Provide simulator here
				System.out.println(">> Will simulate Relay");
				relay.setSimulator(System.out::println);
			}
		} catch (UnsatisfiedLinkError ule) { // That one is trapped in the constructor of RelayDriver.
			System.out.println("You're not on a Raspberry PI, or your wiring is wrong.");
			System.out.println("Exiting.");
			System.exit(1);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			if (relay.getState() == PinState.HIGH) {
				relay.down();
			}
			System.out.println("\nExiting");
			try { Thread.sleep(1_500L); } catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}));

		// Manual input
		Thread manualThread = new Thread(() -> { // TODO A REST input
			while (go) {
				String userInput = StaticUtil.userInput(" T:XX, H:XX > ");
				parseUserInput(userInput);
			}
		});
		manualThread.start();

		/*
		 * This is the main loop
		 */
		while (go) {

			temperature = probe.readTemperature();
			humidity = probe.readHumidity(temperature);

			// TODO A screen (Like the SSD1306), ANSI Console, log file ?
			if (verbose != VERBOSE.ANSI) {
				System.out.println(String.format("Temp: %.02f C, Hum: %.02f%% (dew pt Temp: %.02f C)", temperature, humidity, WeatherUtil.dewPointTemperature(humidity, temperature)));
			} else {
				displayAnsiData();
			}

			/*
			 * Here, test the sensor's values, and make the decision about the valve.
			 */
			if (humidity < humidityThreshold) { // Ah! Need some water
				// Open the valve
				relay.up();
				message = "Watering...";
				if (verbose == VERBOSE.STDOUT) {
					System.out.println(message);
				} else if (verbose == VERBOSE.ANSI) {
					displayAnsiData();
				}
				// Watering time
				try {
					final Thread mainThread = Thread.currentThread();
					final long _waterDuration = wateringDuration;
					Thread wateringThread = new Thread(() -> {
						for (int i=0; i<_waterDuration; i++) {
							try {
								Thread.sleep(1_000L);
							} catch (InterruptedException ie) {
								ie.printStackTrace();
							}
							// Tick, countdown...
							message = String.format("Stop watering in %d sec...", (_waterDuration - i));
							if (verbose == VERBOSE.STDOUT) {
								System.out.println(message);
							} else if (verbose == VERBOSE.ANSI) {
								displayAnsiData();
							}
						}
						synchronized (mainThread) {
							message = "Ok! Enough water!";
							if (verbose == VERBOSE.STDOUT) {
								System.out.println(message);
							} else if (verbose == VERBOSE.ANSI) {
								displayAnsiData();
							}
							mainThread.notify(); // Release the wait on main thread.
						}
					}, "watering-thread");
					wateringThread.start();

					synchronized (mainThread) {
						mainThread.wait();
						message = "";
						if (verbose == VERBOSE.STDOUT) {
							System.out.println(message);
						} else if (verbose == VERBOSE.ANSI) {
							displayAnsiData();
						}
					}
					message = "Shutting off the valve.";
					if (verbose == VERBOSE.STDOUT) {
						System.out.println(message);
					} else if (verbose == VERBOSE.ANSI) {
						displayAnsiData();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				message = "Done watering.";
				if (verbose == VERBOSE.STDOUT) {
					System.out.println(message);
				} else if (verbose == VERBOSE.ANSI) {
					displayAnsiData();
				}
				// Shut the valve
				relay.down();
				// Wait before resuming sensor watching

				message = "Napping a bit... Spreading the word...";
				if (verbose == VERBOSE.STDOUT) {
					System.out.println(message);
				} else if (verbose == VERBOSE.ANSI) {
					displayAnsiData();
				}
				try {
					final Thread mainThread = Thread.currentThread();
					final long _napDuration = resumeSensorWatchAfter;
					Thread wateringThread = new Thread(() -> {
						for (int i = 0; i < _napDuration; i++) {
							try {
								Thread.sleep(1_000L);
							} catch (InterruptedException ie) {
								ie.printStackTrace();
							}
							// Tick, countdown...
							message = String.format("Resuming watching in %s...", fmtDHMS(msToHMS(((_napDuration - i) * 1_000L))));
							if (verbose == VERBOSE.STDOUT) {
								System.out.println(message);
							} else if (verbose == VERBOSE.ANSI) {
								displayAnsiData();
							}
						}
						synchronized (mainThread) {
							message = "Resuming watching.";
							if (verbose == VERBOSE.STDOUT) {
								System.out.println(message);
							} else if (verbose == VERBOSE.ANSI) {
								displayAnsiData();
							}
							mainThread.notify(); // Release the wait on main thread.
						}
					}, "watering-thread");
					wateringThread.start();

					synchronized (mainThread) {
						mainThread.wait();
						message = "";
						if (verbose == VERBOSE.STDOUT) {
							System.out.println(message);
						} else if (verbose == VERBOSE.ANSI) {
							displayAnsiData();
						}
					}
				} catch (InterruptedException ie) {
					ie.printStackTrace();
				}
				// Resume watching
				message = "";
				if (verbose == VERBOSE.ANSI) {
					displayAnsiData();
				}
			} else {
				try {
					Thread.sleep(1_000L);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		if (verbose == VERBOSE.ANSI) {
			AnsiConsole.systemUninstall();
		}

		if (probe.isSimulating()) {
			System.out.println(String.format("Simulated temperature between %.02f and %.02f", minSimTemp, maxSimTemp));
			System.out.println(String.format("Simulated humidity between %.02f and %.02f", minSimHum, maxSimHum));
		}

		probe.shutdownGPIO();
		relay.shutdownGPIO();

		System.out.println("Bye-bye!");
	}
}
