package fona.rxtxmanager.sample;

import fona.rxtxmanager.FONAClient;
import fona.rxtxmanager.FONAManager;

import fona.rxtxmanager.FONAManager.NetworkStatus;
import util.TextToSpeech;

import java.io.IOException;

public class FonaListener implements FONAClient {
	private static FONAManager fona;

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
		System.out.println("From " + sms.getFrom() + ", " + sms.getMessLen() + " char : " + sms.getContent());
		// TODO: This is where you would parse the message and take the appropriate action.

//	String mess = "Message from " + sms.getFrom() + ", " + sms.getContent();
		String mess = sms.getContent();
		TextToSpeech.speak(mess);
		// Echo to sender
		sendResponse("You just said: " + sms.getContent(), sms.getFrom());
	}

	/**
	 * Use this method to send an SMS.
	 * @param messContent Message content
	 * @param sendTo Message destination
	 */
	public void sendResponse(final String messContent, final String sendTo) {
		Thread senderThread = new Thread() {
			public void run() {
				try {
					fona.sendSMS(sendTo, messContent);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		};
		senderThread.start();
	}

	@Override
	public void someoneCalling() {
		System.out.println("Dring dring! Pouet pouet pouet!");
	}

	/**
	 * The main.
	 *
	 * Supported System variables:
	 * <b>serial.port</b>: A stgring like "/dev/ttyUSB0". Default is "/dev/ttyUSB0"
	 * <b>baud.rate</b>: As an integer like 1200, 4800, 9600, etc. Default is 9600.
	 *
	 * @param args Can be used to pass the Baud Rate prm, as an integer. Overrides the "baud.rate" system variable.
	 * @throws NumberFormatException if baud rate passed as a parameter or system variable is invalid (i.e. not an integer).
	 */
	public static void main(String... args)
			throws NumberFormatException {
		FonaListener fonaListener = new FonaListener();
		fona = new FONAManager(fonaListener);

		FONAManager.setVerbose("true".equals(System.getProperty("fona.verbose")));

		String port = System.getProperty("serial.port", "/dev/ttyUSB0");
		int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));
		if (args.length > 0) {
			try {
				br = Integer.parseInt(args[0]);
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
			}
		}

		System.out.println("Serial Communication.");
		System.out.println(" ... connect using port " + port + " : " + Integer.toString(br)); // +  ", N, 8, 1.");
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
		} catch (Exception ex) {
			System.out.println(" ==>> Serial Setup failed : " + ex.getMessage() + " <<== ");
		}
		System.exit(0);
	}
}
