package main;

import com.pi4j.io.gpio.PinState;
import http.HTTPServer;
import loggers.DataLoggerInterface;
import loggers.LogData;
import org.fusesource.jansi.AnsiConsole;
import relay.RelayDriver;
import sensors.sth10.STH10Driver;
import utils.PinUtil;
import utils.StaticUtil;
import utils.WeatherUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static utils.TimeUtil.fmtDHMS;
import static utils.TimeUtil.msToHMS;

/**
 * Example / Working prototype...
 */
public class STH10 implements Probe {

	private static boolean go = true; // Keep looping.

	private final static int DEFAULT_HUMIDITY_THRESHOLD = 50; // 50 %
	private final static long DEFAULT_WATERING_DURATION = 10L; // 10 seconds
	private final static long DEFAULT_RESUME_SENSOR_WATCH_AFTER = 120L; // 120 seconds, 2 minutes

	private final static long DEFAULT_LOGGING_PACE = 10_000L; // 10 seconds

	// Default values
	private static int humidityThreshold = DEFAULT_HUMIDITY_THRESHOLD;
	private static long wateringDuration = DEFAULT_WATERING_DURATION;
	private static long resumeSensorWatchAfter = DEFAULT_RESUME_SENSOR_WATCH_AFTER;

	private static boolean withRESTServer = false;
	private static int restServerPort = 9999; // This is the default value
	private static boolean enforceSensorSimulation = false;

	private static long loggingPace = DEFAULT_LOGGING_PACE;

	private static Long lastWatering = null;

	// Program arguments
	private enum ARGUMENTS {
		HUMIDITY_THRESHOLD("--water-below:", // %
				String.format("Integer. Humidity threshold in %%, default is --water-below:%d, start watering below this value.", DEFAULT_HUMIDITY_THRESHOLD)),
		WATERING_DURATION("--water-during:", // seconds
				String.format("Integer. In seconds, default is --water-during:%d. Duration of the watering process.", DEFAULT_WATERING_DURATION)),
		RESUME_AFTER("--resume-after:", // seconds
				String.format("Integer. In seconds, default is --resume-after:%d. After watering, resume sensor monitoring after this amount of time.", DEFAULT_RESUME_SENSOR_WATCH_AFTER)),
		VERBOSE("--verbose:",
				"String. Verbose, default is --verbose:NONE, values can be 'NONE', 'STDOUT' or 'ANSI'."),

		// STH10pins
		DATA_PIN("--data-pin:", // default is BCM 18 => GPIO_01
				"Integer. BCM (aka GPIO) pin number of the DATA pin of the sensor. Default is --data-pin:18."),
		CLOCK_PIN("--clock-pin:", // default is BCM 23 => GPIO_04
				"Integer. BCM (aka GPIO) pin number of the CLOCK pin of the sensor. Default is --clock-pin:23."),

		// Relay Pin, for the pump or valve
		RELAY_PIN("--relay-pin:",  // default is BCM 17 => GPIO_00
				"Integer. BCM (aka GPIO) pin number of the SIGNAL pin of the RELAY. Default is --relay-pin:17."),

		// REST Interface
		WITH_REST_SERVER("--with-rest-server:",
				"Boolean. Default 'false', starts a REST server is set to 'true'"),
		HTTP_PORT("--http-port:",
				String.format("Integer. The HTTP port of the REST Server. Default is %d.", restServerPort)),

		SIMULATE_SENSOR_VALUES("--simulate-sensor-values:",
				"Boolean. Enforce sensor values simulation, even if running on a Raspberry Pi. Default is 'false'. Note: Relay is left alone."),
		LOGGERS("--loggers:",
				"Comma-separated list of the loggers. Loggers must implement DataLoggerInterface. Ex: --loggers:loggers.iot.AdafruitIOClient,loggers.text.FileLogger "),
		LOGGING_PACE("--logging-pace:",
				String.format("Long, in milliseconds. The interval between each log entry. Default is %d.", DEFAULT_LOGGING_PACE)),
		HELP("--help", "Display the help and exit.");

		private final String prefix, help;

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

	private static STH10Driver probe = null;
	private static RelayDriver relay = null;

	// Simulators, to run on non-Raspberry Pis - for development and tests.
	// User manual entry (also suitable for REST)
	private static Supplier<Double> temperatureSimulator = STH10::simulateUserTemp;
	private static Supplier<Double> humiditySimulator = STH10::simulateUserHum;
	// Random values
//	private static Supplier<Double> temperatureSimulator = STH10::simulateTemp;
//	private static Supplier<Double> humiditySimulator = STH10::simulateHum;

	private static double temperature = 20d;
	private static double humidity = 80d;
	private static String message = "";

	private static double minSimTemp = temperature, maxSimTemp = temperature;
	private static double minSimHum = humidity, maxSimHum = humidity;

	private static PinState simulatedPinState = PinState.HIGH;
	private static PinState actualPinState = PinState.HIGH;
	private final static Supplier<PinState> relaySignalSimulator = () -> simulatedPinState;
	private final static Consumer<PinState> relayObserver = state -> {
		System.out.println(">> Relay is now " + state);
		simulatedPinState = state;
	};
	private final static Consumer<PinState> relayListener = state -> actualPinState = state;

	private static HTTPServer httpServer = null;

	// Loggers
	private final static List<DataLoggerInterface> loggers = new ArrayList<>(); //Arrays.asList(new AdafruitIOClient()); // Example
	private static long lastLog = -1;

	// Sensor data Getters and Setters, for (optional) REST
	@Override
	public void setTemperature(double temp) {
		temperature = temp;
	}

	@Override
	public void resumeWatering() {
		// Duh
	}

	@Override
	public void setHumidity(double hum) {
		humidity = hum;
	}

	@Override
	public double getTemperature() {
		return temperature;
	}
	@Override
	public double getHumidity() {
		return humidity;
	}

	@Override
	public double getRawHumidity() {
		return 0;
	}


	@Override
	public PinState getRelayState() {
		PinState state = null;
		if (relay != null) {
			state = relay.getState();
		}
		return state;
	}

	@Override
	public void setRelayState(PinState state) {
		if (relay != null) {
			Thread relayFlip = null;
			if (state.isHigh()) {
				relayFlip = new Thread(() -> {
					synchronized (relay) {
						relay.off();
					}
				});
			} else {
				relayFlip = new Thread(() -> {
					synchronized (relay) {
						relay.on();
					}
				});
			}
			relayFlip.start();
		}
	}

	@Override
	public Long getLastWateringTime() {
		return lastWatering;
	}

	@Override
	public PWSParameters getPWSParameters() {
		return new PWSParameters()
				.humidityThreshold(humidityThreshold)
				.wateringTime(wateringDuration)
				.resumeWatchAfter(resumeSensorWatchAfter);
	}

	@Override
	public void setPWSParameters(PWSParameters pwsParameters) {
		if (pwsParameters.humidityThreshold() != -1) {
			humidityThreshold = pwsParameters.humidityThreshold();
		}
		if (pwsParameters.wateringTime() != -1) {
			wateringDuration = pwsParameters.wateringTime();
		}
		if (pwsParameters.resumeWatchAfter() != -1) {
			resumeSensorWatchAfter = pwsParameters.resumeWatchAfter();
		}
	}

	@Override
	public String getStatus() {
		return message;
	}

	@Override
	public	List<Double> getRecentData() {
		return null;
	}

	private static double randomDiff() {
		int sign = (int)System.currentTimeMillis() % 2;
		return Math.random() * (sign == 0 ? 1 : -1);
	}

	private static Double simulateTemp() {
		temperature += randomDiff();
		minSimTemp = Math.min(minSimTemp, temperature);
		maxSimTemp = Math.max(maxSimTemp, temperature);
		return temperature;
	}

	private static Double simulateHum() {
		humidity += randomDiff();
		minSimHum = Math.min(minSimHum, humidity);
		maxSimHum = Math.max(maxSimHum, humidity);
		return humidity;
	}

	private static void displayANSIConsole() {
		ANSIUtil.displayAnsiData(
				humidityThreshold,
				wateringDuration,
				resumeSensorWatchAfter,
				temperature,
				humidity,
				message,
				withRESTServer,
				restServerPort,
				lastWatering,
				(relay != null && relay.isSimulating() ? simulatedPinState : actualPinState));
	}
	// Interactive simulators, for dev and tests.
	private static Double simulateUserTemp() {
		return temperature;
	}
	private static Double simulateUserHum() {
		return humidity;
	}

	// Parse manual user input, for simulation
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

	private static boolean watchTheProbe = true;

	public static void main(String... args) {

		// Defaults
		int dataPin = 18,
				clockPin = 23,
				relayPin = 17;

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
			} else if (arg.startsWith(ARGUMENTS.WITH_REST_SERVER.prefix())) {
				String val = arg.substring(ARGUMENTS.WITH_REST_SERVER.prefix().length());
				withRESTServer = "true".equals(val);
			} else if (arg.startsWith(ARGUMENTS.HTTP_PORT.prefix())) {
				String val = arg.substring(ARGUMENTS.HTTP_PORT.prefix().length());
				try {
					restServerPort = Integer.parseInt(val);
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(ARGUMENTS.SIMULATE_SENSOR_VALUES.prefix())) {
				String val = arg.substring(ARGUMENTS.SIMULATE_SENSOR_VALUES.prefix().length());
				enforceSensorSimulation = "true".equals(val);
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
						humidityThreshold = DEFAULT_HUMIDITY_THRESHOLD;
						System.err.printf(">> Humidity Threshold must be in [0..100]. Reseting to %d \n", DEFAULT_HUMIDITY_THRESHOLD);
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(ARGUMENTS.WATERING_DURATION.prefix())) {
				String val = arg.substring(ARGUMENTS.WATERING_DURATION.prefix().length());
				try {
					wateringDuration = Long.parseLong(val);
					if (wateringDuration < 0) {
						wateringDuration = DEFAULT_WATERING_DURATION;
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
						resumeSensorWatchAfter = DEFAULT_RESUME_SENSOR_WATCH_AFTER;
						System.err.println(">> Resume Watch After must be positive. Ignoring.");
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else if (arg.startsWith(ARGUMENTS.LOGGERS.prefix())) {
				String val = arg.substring(ARGUMENTS.LOGGERS.prefix().length());
				String[] logConsumers = val.split(",");
				for (String oneLogger : logConsumers) {
					try {
						Class<?> logClass = Class.forName(oneLogger);
						Object consumer = logClass.getConstructor().newInstance();
						loggers.add(DataLoggerInterface.class.cast(consumer));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			} else if (arg.startsWith(ARGUMENTS.LOGGING_PACE.prefix())) {
				String val = arg.substring(ARGUMENTS.LOGGING_PACE.prefix().length());
				try {
					loggingPace = Long.parseLong(val);
					if (loggingPace < 0) {
						loggingPace = DEFAULT_LOGGING_PACE;
						System.err.println(">> Logging Pace must be positive. Ignoring.");
					}
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			}
		}
		if (verbose == VERBOSE.ANSI) {
			AnsiConsole.systemInstall();
			AnsiConsole.out.println(ANSIUtil.ANSI_CLS);
		}
		// Print summary
		if (verbose == VERBOSE.ANSI) {
			displayANSIConsole();
		} else {
			System.out.println("+------- P L A N T   W A T E R I N G   S Y S T E M --------");
			System.out.printf("| Start watering under %d%% of humidity.\n", humidityThreshold);
			System.out.printf("| Water during %s\n", fmtDHMS(msToHMS(wateringDuration * 1_000)));
			System.out.printf("| Resume sensor watch %s after watering.\n", fmtDHMS(msToHMS(resumeSensorWatchAfter * 1_000)));
			if (withRESTServer) {
				System.out.printf("| REST Server running on port %d.\n", restServerPort);
			}
			System.out.println("+----------------------------------------------------------");
		}

		if (verbose == VERBOSE.STDOUT) {
			System.out.println("Wiring:");
			// Compose mapping for PinUtil, physical numbers.
			String[] map = new String[3];
			map[0] = (PinUtil.findByPin(PinUtil.getPinByGPIONumber(dataPin)).pinNumber()) + ":" + "DATA";
			map[1] = (PinUtil.findByPin(PinUtil.getPinByGPIONumber(clockPin)).pinNumber()) + ":" + "CLOCK";
			map[2] = (PinUtil.findByPin(PinUtil.getPinByGPIONumber(relayPin)).pinNumber()) + ":" + "RELAY";
			PinUtil.print(map);
		}

		STH10 instance = new STH10();
		try {
			probe = new STH10Driver(
					PinUtil.getPinByGPIONumber(dataPin),
					PinUtil.getPinByGPIONumber(clockPin));
			if (probe.isSimulating() || enforceSensorSimulation) {
				// Provide simulator here
				System.out.printf(">> Will simulate STH10%s\n", (enforceSensorSimulation ? " (enforced)" : ""));
				if ("true".equals(System.getProperty("random.simulator"))) {
					temperatureSimulator = STH10::simulateTemp;
					humiditySimulator = STH10::simulateHum;
				} else { // User input
					temperatureSimulator = STH10::simulateUserTemp;
					humiditySimulator = STH10::simulateUserHum;
				}
				probe.setSimulators(temperatureSimulator, humiditySimulator);
			}
	  } catch (UnsatisfiedLinkError ule) { // That one is trapped in the constructor of STH10Driver.
			System.out.println("You're not on a Raspberry Pi, or your wiring is wrong.");
			System.out.println("Exiting.");
			System.exit(1);
		}
		try {
			relay = new RelayDriver(PinUtil.getPinByGPIONumber(relayPin));
			if (relay.isSimulating()) {
				// Provide simulator here
				System.out.println(">> Will simulate Relay");
				relay.setSimulator(relayObserver, relaySignalSimulator);
			} else {
				// Relay listener
				relay.setListener(relayListener);
			}
		} catch (UnsatisfiedLinkError ule) { // That one is trapped in the constructor of RelayDriver.
			System.out.println("You're not on a Raspberry Pi, or your wiring is wrong.");
			System.out.println("Exiting.");
			System.exit(1);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			if (relay.getState() == PinState.LOW) {
				relay.off();
			}
			System.out.println("\nExiting (Main Hook)");

			loggers.forEach(DataLoggerInterface::close);

			probe.shutdownGPIO();
			relay.shutdownGPIO();
			try { Thread.sleep(1_500L); } catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}, "Shutdown Hook"));

		if ((probe.isSimulating() || enforceSensorSimulation) && !"true".equals(System.getProperty("random.simulator"))) {
			// Manual input, stdin.
			Thread manualThread = new Thread(() -> { // There is also a REST input
				while (go) {
					String userInput = StaticUtil.userInput(" T:XX, H:XX > ");
					parseUserInput(userInput);
				}
			}, "manual-input");
			manualThread.start();
		}

		if (withRESTServer) {
			// HTTP Server + REST Request Manager
			httpServer = new RequestManager(instance).startHttpServer(restServerPort);
		}

		// Open/Close valve, for test
		if ("true".equals(System.getProperty("valve.test"))) {
			System.out.println("Testing the valve");
			synchronized (relay) {
				relay.on();
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException ie) {
				}
				relay.off();
			}
			System.out.println("Valve test completed.");
		}

		/*
		 * This is the main loop
		 */
		if (verbose == VERBOSE.STDOUT) { // Can be used for logging
			System.out.println("-- LOGGING STARTS HERE --");
			System.out.println("Epoch(ms);Date;Temp(C);Hum(%);Dew-pt Temp(C)");
		}
		while (go) {

			if (!enforceSensorSimulation) {
				try {
					temperature = probe.readTemperature();
				} catch (Exception ex) {
					System.err.printf("At %s :\n", new Date().toString());
					ex.printStackTrace();
					probe.softReset();
					System.err.println("Device was reset");
				}
				try {
					humidity = probe.readHumidity(temperature);
				} catch (Exception ex) {
					System.err.printf("At %s :\n", new Date().toString());
					ex.printStackTrace();
					probe.softReset();
					System.err.println("Device was reset");
				}
			}

			long now = System.currentTimeMillis();
			if (loggers.size() > 0 && (now - lastLog) > loggingPace) {
				lastLog = now;
				loggers.forEach(logger -> {
					Thread loggerThread = new Thread(() -> {
						try {
							logger.accept(new LogData()
									.feed(LogData.FEEDS.AIR)
									.numValue(temperature));
						} catch (Exception ex) {
							System.err.printf("At %s :\n", new Date().toString());
							System.err.println(ex.toString());
							//	ex.printStackTrace(); // TODO An option to get the full stacktrace?
						}
						try {
							logger.accept(new LogData()
									.feed(LogData.FEEDS.HUM)
									.numValue(humidity));
						} catch (Exception ex) {
							System.err.printf("At %s :\n", new Date().toString());
							System.err.println(ex.toString());
							//	ex.printStackTrace(); // TODO An option to get the full stacktrace?
						}
					});
					loggerThread.start();
				});
			}

			if (verbose == VERBOSE.STDOUT) { // Can be used for logging
				System.out.printf("%d;%s;%.02f;%.02f;%.02f\n",
						System.currentTimeMillis(),
						new Date().toString(),
						temperature,
						humidity,
						WeatherUtil.dewPointTemperature(humidity, temperature));
			} else if (verbose == VERBOSE.ANSI) {
				displayANSIConsole();
			}

			/*
			 * Here, test the sensor's values, and make the decision about the valve.
			 */
			if (watchTheProbe) {
				message = "Watching the probe..."; // Default value
			}
			/*
			 * the watchTheProbe variable is used to nap after watering.
			 */
			if (watchTheProbe && humidity < humidityThreshold) { // Ah! Need some water
				// Open the valve
				Thread waterRelayOn = new Thread(() -> {
					synchronized (relay) {
						relay.on();
					}
				});
				waterRelayOn.start();
				message = "Watering...";
				if (verbose == VERBOSE.STDOUT) {
					System.out.println(message);
				} else if (verbose == VERBOSE.ANSI) {
					displayANSIConsole();
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
								Thread.currentThread().interrupt();
							}
							// Tick, countdown...
							message = String.format("Stop watering in %d sec...", (_waterDuration - i));
							if (verbose == VERBOSE.STDOUT) {
								System.out.println(message);
							} else if (verbose == VERBOSE.ANSI) {
								displayANSIConsole();
							}
						}
						synchronized (mainThread) {
							message = "Ok! Enough water!";
							if (verbose == VERBOSE.STDOUT) {
								System.out.println(message);
							} else if (verbose == VERBOSE.ANSI) {
								displayANSIConsole();
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
							displayANSIConsole();
						}
					}
					message = "Shutting off the valve.";
					lastWatering = System.currentTimeMillis();
					if (verbose == VERBOSE.STDOUT) {
						System.out.println(message);
					} else if (verbose == VERBOSE.ANSI) {
						displayANSIConsole();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				message = "Done watering.";
				if (verbose == VERBOSE.STDOUT) {
					System.out.println(message);
				} else if (verbose == VERBOSE.ANSI) {
					displayANSIConsole();
				}
				// Shut the valve
				Thread waterRelayOff = new Thread(() -> {
					synchronized (relay) {
						relay.off();
					}
				});
				waterRelayOff.start();

				// Wait before resuming sensor watching

				message = "Napping a bit... Spreading the word...";
				watchTheProbe = false;
				if (verbose == VERBOSE.STDOUT) {
					System.out.println(message);
				} else if (verbose == VERBOSE.ANSI) {
					displayANSIConsole();
				}
				try {
					final Thread mainThread = Thread.currentThread();
					final long _napDuration = resumeSensorWatchAfter;
					Thread wateringThread = new Thread(() -> {
						for (int i = 0; i < _napDuration; i++) {
							try {
								Thread.sleep(1_000L);
							} catch (InterruptedException ie) {
								Thread.currentThread().interrupt();
							}
							// Tick, countdown...
							message = String.format("Resuming watch in %s...", fmtDHMS(msToHMS(((_napDuration - i) * 1_000L))));
							if (verbose == VERBOSE.STDOUT) {
								System.out.println(message);
							} else if (verbose == VERBOSE.ANSI) {
								displayANSIConsole();
							}
						}
						synchronized (mainThread) {
							message = "Resuming watch.";
							if (verbose == VERBOSE.STDOUT) {
								System.out.println(message);
							} else if (verbose == VERBOSE.ANSI) {
								displayANSIConsole();
							}
							watchTheProbe = true; // Resume!
			//			mainThread.notify(); // Release the wait on main thread.
						}
					}, "wait-for-resume-thread");
					wateringThread.start();

					synchronized (mainThread) {
			//		mainThread.wait(); // Wait for resume (above)
						message = "";
						if (verbose == VERBOSE.STDOUT) {
							System.out.println(message);
						} else if (verbose == VERBOSE.ANSI) {
							displayANSIConsole();
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
//			} catch (InterruptedException ie) {
//				Thread.currentThread().interrupt();
				}
				// Resume watching
				message = "...";
				if (verbose == VERBOSE.ANSI) {
					displayANSIConsole();
				}
			} else {
				try {
					Thread.sleep(1_000L);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
		}

		if (withRESTServer) {
			if (httpServer.isRunning()) {
				httpServer.stopRunning();
			}
		}

		if (verbose == VERBOSE.ANSI) {
			AnsiConsole.systemUninstall();
		}

		if (probe.isSimulating()) {
			System.out.printf("Simulated temperature between %.02f and %.02f\n", minSimTemp, maxSimTemp);
			System.out.printf("Simulated humidity between %.02f and %.02f\n", minSimHum, maxSimHum);
		}

		probe.shutdownGPIO();
		relay.shutdownGPIO();

		System.out.println("Bye-bye!");
	}
}
