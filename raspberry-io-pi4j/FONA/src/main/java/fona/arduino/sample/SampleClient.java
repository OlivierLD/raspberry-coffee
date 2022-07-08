package fona.arduino.sample;

import com.pi4j.io.serial.SerialPortException;
import fona.arduino.FONAClient;
import fona.arduino.ReadWriteFONA;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static utils.StaticUtil.userInput;

public class SampleClient
		implements FONAClient {
	public SampleClient() {
		super();
	}

	@Override
	public void sendSuccess(String dummy) {
		System.out.println("Message sent successfully.");
		if (wait4ack != null) {
			synchronized (wait4ack) {
				wait4ack.notify();
				System.out.println("  Waiter notified.");
			}
		} else {
			System.out.println("... wierd. No waiter for Ack.");
		}
	}

	@Override
	public void ready() {
		System.out.println("\nReady for duty.");
	}

	@Override
	public void genericSuccess(String mess) {
		System.out.println("\n" + "Generic success:" + mess);
	}

	@Override
	public void genericFailure(String mess) {
		System.out.println("\n" + "Generic failure:" + mess);
	}

	@Override
	public void batteryState(String mess) {
		System.out.println("\n" + mess);
	}

	@Override
	public void adcState(String mess) {
		System.out.println("\n" + mess);
	}

	@Override
	public void ccidState(String mess) {
		System.out.println("\n" + mess);
	}

	@Override
	public void rssiState(String mess) {
		System.out.println("\n" + mess);
	}

	@Override
	public void networkState(String mess) {
		System.out.println("\n" + mess);
	}

	@Override
	public void numberOfMessages(int nb) {
		System.out.println("\n# of messages:" + nb);
	}

	@Override
	public void message(ReadWriteFONA.SMS sms) {
		System.out.println("Message # " + sms.getNum() + ", from " +
				sms.getFrom() + ", " +
				sms.getLen() + " char(s), " +
				sms.getContent());
	}

	private static Thread wait4ack = null;

	private static void sendAndWait4Ack(ReadWriteFONA fona, String to, String content) throws IOException {
		fona.sendMess(to, content);
		wait4ack = Thread.currentThread();
		synchronized (wait4ack) {
			try {
				wait4ack.wait(5_000L);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			System.out.println("... Released!");
		}
		wait4ack = null;
	}

	/**
	 * A Sample Command Line Interface (CLI) for FONA/Arduino
	 *
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String... args) throws InterruptedException, IOException {
		if (args.length > 0) {
			System.out.print("Called with");
			for (String s : args)
				System.out.println(" " + s);
			System.out.println();
		}
		SampleClient client = new SampleClient();
		final ReadWriteFONA fona = new ReadWriteFONA(client);
		fona.openSerialInput();
		fona.startListening();

		try {
			System.out.println("Hit 'Q' to quit.");
//    System.out.println("Hit [return] when ready to start.");
//    userInput("");

			final Thread me = Thread.currentThread();
			Thread userInputThread = new Thread() {
				public void run() {
					displayMenu();
					boolean loop = true;
					while (loop) {
						String userInput = "";
						userInput = userInput("So? > ");
						if ("Q".equalsIgnoreCase(userInput))
							loop = false;
						else {
							try {
								//  channel.sendSerial(userInput); // Private
								if ("?".equals(userInput)) {
									displayMenu();
								} else if ("b".equals(userInput)) {
									fona.requestBatteryState();
								} else if ("a".equals(userInput)) {
									fona.requestADC();
								} else if ("C".equals(userInput)) {
									fona.requestSIMCardNumber();
								} else if ("i".equals(userInput)) {
									fona.requestRSSI();
								} else if ("n".equals(userInput)) {
									fona.requestNetworkStatus();
								} else if ("N".equals(userInput)) {
									fona.requestNumberOfMessage();
								} else if ("r".equals(userInput)) {
									String _smsn = userInput("Mess #:");
									fona.readMessNum(Integer.parseInt(_smsn));
								} else if ("d".equals(userInput)) {
									String _smsn = userInput("Mess #:");
									fona.deleteMessNum(Integer.parseInt(_smsn));
								} else if ("s".equals(userInput)) {
									String to = userInput("Send to > ");
									String payload = userInput("Message content > ");
									sendAndWait4Ack(fona, to, payload);
								} else {
									System.out.println("Duh?");
								}
							} catch (IOException ioe) {
								ioe.printStackTrace();
							}
						}
					}
					synchronized (me) {
						me.notify();
					}
				}
			};
			userInputThread.start();

			synchronized (me) {
				me.wait();
			}
			System.out.println("Bye!");
			fona.closeChannel();
		} catch (SerialPortException ex) {
			System.out.println(" ==>> Serial Setup failed : " + ex.getMessage());
			return;
		}
		System.exit(0);
	}

	private static void displayMenu() {
		System.out.println("\nCommands are case-sensitive.");
		System.out.println("[?] Display menu");
		System.out.println("[Q or q] to quit");
		System.out.println("[a] ADC");
		System.out.println("[b] Battery");
		System.out.println("[C] Read SIM Card #");
		System.out.println("[i] Read RSSI");
		System.out.println("[n] Network status");
		System.out.println("[N] Number of messages");
		System.out.println("[r] Read message");
		System.out.println("[d] Delete message");
		System.out.println("[s] Send message");
	}
}
