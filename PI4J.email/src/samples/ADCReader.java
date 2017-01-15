package samples;

import adc.ADCObserver;
import adc.sample.BatteryMonitor;
import pi4j.email.EmailSender;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

public class ADCReader {
	private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));
	private final static NumberFormat VOLT_FMT = new DecimalFormat("#00.00");

	private float voltage = 0f;
	private boolean keepGoing = true;

	public void consumer(BatteryMonitor.ADCData adcData) {
		this.voltage = adcData.getVoltage();
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
	 * java samples.ADCReader [-verbose] -send:google -sendto:me@home.net
	 *
	 * TODO Put all prms in the properties file.
	 *
	 * @param args use -help
	 */
	public static void main(String[] args)
	throws Exception {

		final long SECOND = 1000L;
		final long MINUTE = 60 * SECOND;
		final long HOUR = 60 * MINUTE;

		final long BETWEEN_LOOPS = 24 * HOUR;

		String providerSend = "yahoo"; // Default
		String sendTo = "";
		String[] dest = null;

		for (int i = 0; i < args.length; i++) {
			if ("-verbose".equals(args[i])) {
				verbose = true;
				System.setProperty("verbose", "true");
			} else if (args[i].startsWith("-send:"))
				providerSend = args[i].substring("-send:".length());
			else if (args[i].startsWith("-sendto:")) {
				sendTo = args[i].substring("-sendto:".length());
				dest = sendTo.split(",");
			} else if ("-help".equals(args[i])) {
				System.out.println("Usage:");
				System.out.println("  java samples.ADCReader -verbose -send:google sendto:me@home.net,you@yourplace.biz -help");
				System.exit(0);
			}
		}
		if (dest == null || dest.length == 0 || dest[0].trim().length() == 0) {
			throw new RuntimeException("No destination email. Use the help.");
		}

		ADCReader adcReader = new ADCReader();
		if (verbose) {
			System.out.println("Creating BatteryMonitor...");
		}
		BatteryMonitor batteryMonitor = new BatteryMonitor(ADCObserver.MCP3008_input_channels.CH0.ch());
		if (verbose) {
			System.out.println("Creating BatteryMonitor: done");
		}
		batteryMonitor.setProcessor(adcReader::consumer);

		final String[] destEmail = dest;
		final EmailSender sender = new EmailSender(providerSend);
		final Thread senderThread = new Thread() {
			public void run() {
				while (adcReader.keepGoing()) {
					try {
						System.out.println("Sending...");
						String content = "At " + new Date().toString() +
										", voltage was " +
										VOLT_FMT.format(adcReader.getVoltage() + " V.");
						sender.send(destEmail,
										"PiVolt",
										content);
						System.out.println("Sent.");
						this.wait(BETWEEN_LOOPS);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				System.out.println("SenderThread exiting.");
			}
		};
		if (verbose) {
			System.out.println("Staring sender thread");
		}
		senderThread.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				adcReader.stop();
				senderThread.notify();
				System.out.println("Bye now");
			}
		});
	}
}
