package battery.email;

import adc.ADCObserver;
import adc.sample.BatteryMonitor;
import email.EmailSender;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

public class ADCReader {
	private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));
	private final static NumberFormat VOLT_FMT = new DecimalFormat("#00.00");

	private BatteryMonitor batteryMonitor = null;

	public BatteryMonitor getBatteryMonitor() {
		return batteryMonitor;
	}

	public void setBatteryMonitor(BatteryMonitor batteryMonitor) {
		this.batteryMonitor = batteryMonitor;
	}

	private float voltage = 0f;
	private boolean keepGoing = true;

	public void consumer(BatteryMonitor.ADCData adcData) {
		this.voltage = adcData.getVoltage();
		if (verbose) {
			System.out.println(
							String.format("From ADC Observer: volume %d, value %d, voltage %f",
											adcData.getVolume(),
											adcData.getNewValue(),
											adcData.getVoltage()));
		}
	}

	public float getVoltage() {
		return this.voltage;
	}

	public void stop() {
		this.keepGoing = false;
	}

	public boolean keepGoing() {
		return this.keepGoing;
	}

	/**
	 * Invoked like:
	 * java battery.email.ADCReader [-verbose] -send:google -sendto:me@home.net -loop:6h
	 *
	 * TODO Put all prms in the properties file?
	 *
	 * @param args use -help
	 */
	public static void main(String... args)
	throws Exception {

		final long SECOND = 1_000L;
		final long MINUTE = 60 * SECOND;
		final long HOUR = 60 * MINUTE;

		long betweenLoops = 24 * HOUR; // 1 * MINUTE, etc...

		String providerSend = "yahoo"; // Default
		String sendTo = "";
		String[] dest = null;

		for (int i = 0; i < args.length; i++) {
			if ("-verbose".equals(args[i])) {
				verbose = true;
				System.setProperty("verbose", "true");
			} else if (args[i].startsWith("-send:")) {
				providerSend = args[i].substring("-send:".length());
			} else if (args[i].startsWith("-loop:")) {
				String loop = args[i].substring("-loop:".length());
				if (loop.endsWith("h")) {
					long multiplier = HOUR;
					try {
						String val = loop.substring(0, loop.length() - 1);
						betweenLoops = multiplier * Integer.parseInt(val);
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
						System.err.println("Using 24h (default)");
					}
				} else if (loop.endsWith("m")) {
					long multiplier = MINUTE;
					try {
						String val = loop.substring(0, loop.length() - 1);
						betweenLoops = multiplier * Integer.parseInt(val);
					} catch (NumberFormatException nfe) {
						System.err.println(nfe.toString());
						System.err.println("Using 24h (default)");
					}
				} else {
					System.out.println("Unrecognized loop value. Must end with 'h' or 'm'.");
					System.out.println("24h is 24 hours");
					System.out.println("10m is 10 minutes");
					System.out.println("Using 24h (default)");
				}
			} else if (args[i].startsWith("-sendto:")) {
				sendTo = args[i].substring("-sendto:".length());
				dest = sendTo.split(",");
			} else if ("-help".equals(args[i])) {
				System.out.println("Usage:");
				System.out.println("  java battery.email.ADCReader -verbose -send:google -sendto:me@home.net,you@yourplace.biz -loop:24h -help");
				System.exit(0);
			}
		}
		if (dest == null || dest.length == 0 || dest[0].trim().length() == 0) {
			throw new RuntimeException("No destination email. Use the help (option -help).");
		}

		ADCReader adcReader = new ADCReader();

		Thread batteryThread = new Thread(() -> {
				try {
					if (verbose) {
						System.out.println("Creating BatteryMonitor...");
					}
					BatteryMonitor batteryMonitor = new BatteryMonitor(ADCObserver.MCP3008_input_channels.CH0.ch(), adcReader::consumer);
					adcReader.setBatteryMonitor(batteryMonitor);
					if (verbose) {
						System.out.println("Creating BatteryMonitor: done");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		batteryThread.start();

		final long BETWEEN_LOOPS = betweenLoops;
		final String[] destEmail = dest;
		final EmailSender sender = new EmailSender(providerSend);
		final Thread senderThread = new Thread() {
			public void run() {
				while (adcReader.keepGoing()) {
					try {
						System.out.println("Sending...");
						String content = "At " + new Date().toString() +
										", voltage was " +
										VOLT_FMT.format(adcReader.getVoltage()) + " Volts.";
						sender.send(destEmail,
										"PiVolt",
										content);
						System.out.println("Sent.");
						synchronized (this) {
							this.wait(BETWEEN_LOOPS);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				System.out.println("SenderThread exiting.");
			}
		};
		// Wait for the battery monitor to read something...
		try {
			Thread.sleep(5_000);
		} catch (Exception ex) {
		}
		if (verbose) {
			System.out.println("Starting sender thread");
		}
		senderThread.start();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				adcReader.stop();
				synchronized (senderThread) {
					senderThread.notify();
				}
				if (adcReader.getBatteryMonitor() != null) {
					adcReader.getBatteryMonitor().stop();
				}
				System.out.println("Bye now");
			}, "Shutdown Hook"));
	}
}
