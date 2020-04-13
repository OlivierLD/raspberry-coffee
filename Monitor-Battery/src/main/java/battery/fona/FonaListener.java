package battery.fona;

import adc.sample.BatteryMonitor;
import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialPortException;
import fona.pi4jmanager.FONAClient;
import fona.pi4jmanager.FONAManager;
import fona.pi4jmanager.FONAManager.NetworkStatus;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Listens for incoming call (See {@link FONAManager}
 * Receives data from the BatteryMonitor (See {@link BatteryMonitor})
 *
 * When a request-for-voltage (V? or v?) call is received, the voltage is returned.
 */
public class FonaListener implements FONAClient {
	private static boolean verbose = false;
	private static FONAManager fona;

	private BatteryMonitor batteryMonitor = null;
	private final static NumberFormat VOLT_FMT = new DecimalFormat("#00.00");

	public BatteryMonitor getBatteryMonitor() {
		return batteryMonitor;
	}

	public void setBatteryMonitor(BatteryMonitor batteryMonitor) {
		this.batteryMonitor = batteryMonitor;
	}

	private float voltage = 0f;

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

	public static void main(String... args)
					throws InterruptedException, NumberFormatException, IOException {

		verbose = "true".equals(System.getProperty("verbose", "false"));

		FonaListener fonaListener = new FonaListener();
		Thread batteryThread = new Thread(() -> {
			try {
				if (verbose) {
					System.out.println("Creating BatteryMonitor...");
				}
				BatteryMonitor batteryMonitor = new BatteryMonitor(MCPReader.MCP3008InputChannels.CH0.ch(), fonaListener::consumer);
				fonaListener.setBatteryMonitor(batteryMonitor);
				if (verbose) {
					System.out.println("Creating BatteryMonitor: done");
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		batteryThread.start();

		fona = new FONAManager(fonaListener);

		FONAManager.setVerbose(false);

		String port = System.getProperty("serial.port", Serial.DEFAULT_COM_PORT);
		int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));
		if (args.length > 0) {
			try {
				br = Integer.parseInt(args[0]);
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		}

		System.out.println("Serial Communication.");
		System.out.println(" ... connect using port " + port + ":" + Integer.toString(br)); // +  ", N, 8, 1.");
		System.out.println(" ... data received on serial port should be displayed below.");

		try {
			System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
			fona.openSerial(port, br);
			System.out.println("Port is opened.");
			System.out.println("Establishing connection (can take up to 3 seconds).");
			while (!fona.isConnected()) {
				fona.tryToConnect();
				if (!fona.isConnected()) {
					FONAManager.delay(1);
				}
			}
			System.out.println("Connection established.");

			final Thread me = Thread.currentThread();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println();
				synchronized (me) {
					me.notify();
				}
				System.out.println("Program stopped by user's request.");
			}, "Shutdown Hook"));

			synchronized (me) {
				me.wait();
			}
			System.out.println("Bye!");
			fona.stopReading();
			fona.closeSerial();
		} catch (SerialPortException ex) {
			System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage() + " <<== ");
			return;
		}
		System.exit(0);
	}

	@Override
	public void networkStatusResponse(NetworkStatus ns) {
		System.out.println(ns.label());
	}

	@Override
	public void smsDeletedResponse(int sms, boolean ok) {
		System.out.println("Message #" + sms + " deleted:" + (ok ? "OK" : "Failed"));
	}

	@Override
	public void receivedSMS(final int sms) {
		if (verbose) {
			System.out.println("Received mess #" + sms);
		}
		Thread readit = new Thread(() -> {
			try {
				fona.readMessNum(sms);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		});
		readit.start();

		Thread deleteit = new Thread(() -> {
			FONAManager.delay(10f);
			System.out.println("\t\t>>>> Deleting mess #" + sms);
			try {
				fona.deleteSMS(sms);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		});
		deleteit.start();
	}

	@Override
	public void fonaConnected() {
		System.out.println("FONA Connected!");
	}

	@Override
	public void moduleNameAndRevision(String str) {
		System.out.println("Module:" + str);
	}

	@Override
	public void debugOn() {
		System.out.println("Debug ON");
	}

	@Override
	public void batteryResponse(String percent, String mv) {
		System.out.println("Load:" + percent + "%, " + mv + " mV");
	}

	@Override
	public void signalResponse(String s) {
		System.out.println("Signal:" + s + " dB. Must be higher than 5, the higher the better.");
	}

	@Override
	public void simCardResponse(String s) {
		System.out.println("SIM Card # " + s);
	}

	@Override
	public void networkNameResponse(String s) {
		System.out.println("Network:" + s);
	}

	@Override
	public void numberSMSResponse(int n) {
		System.out.println("Number of SMS :" + n);
	}

	@Override
	public void readSMS(FONAManager.ReceivedSMS sms) {
		if (verbose) {
			System.out.println("From " + sms.getFrom() + ", " + sms.getMessLen() + " char : " + sms.getContent());
		}
		String mess = sms.getContent();
		if ("V?".equalsIgnoreCase(mess)) {
			String response = String.format("Voltage is %s.", VOLT_FMT.format(getVoltage()));
			sendResponse(response, sms.getFrom());
		} else {
			// Echo to sender
			sendResponse("You just said: " + sms.getContent(), sms.getFrom());
		}
	}

	/**
	 * Use this method to send an SMS.
	 * @param messContent Message content
	 * @param sendTo Message destination
	 */
	public void sendResponse(final String messContent, final String sendTo) {
		Thread senderThread = new Thread(() -> {
			try {
				fona.sendSMS(sendTo, messContent);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		});
		senderThread.start();
	}

	@Override
	public void someoneCalling() {
		System.out.println("Dring dring! Pouet pouet pouet!");
	}
}
