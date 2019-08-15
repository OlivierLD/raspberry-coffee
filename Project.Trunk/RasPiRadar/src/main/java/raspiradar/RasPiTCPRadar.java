package raspiradar;

import com.pi4j.io.i2c.I2CFactory;
import utils.PinUtil;
import utils.TimeUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Minimal user interface.
 *
 * This is an illustration of the way to use the {@link RasPiRadar}, and spit out the data to a terminal console.
 */
public class RasPiTCPRadar {

	private static boolean verbose = "true".equals(System.getProperty("radar.verbose"));

	private static final int BUFFER_LENGTH = 10;
	private static List<Double> buffer = new ArrayList<>(BUFFER_LENGTH);

	private final static String PCA9685_SERVO_PORT = "--servo-port:";
	private final static String DELAY              = "--delay:";
	private final static String TRIGGER_PIN        = "--trigger-pin:";
	private final static String ECHO_PIN           = "--echo-pin:";
	private final static String JUST_RESET         = "--just-reset";
	private final static String JUST_ONE_LOOP      = "--just-one-loop";

	private static boolean loop = true;
	private static long delay = 100L;
	private static boolean justReset = false;
	private static boolean justOneLoop = false;

	private List<Socket> clientSocketlist = new ArrayList<>(1);
	private int tcpPort = 7001;
	private ServerSocket serverSocket = null;

	private class SocketThread extends Thread {
		private RasPiTCPRadar parent = null;

		public SocketThread(RasPiTCPRadar parent) {
			super("TCPServer");
			this.parent = parent;
		}

		public void run() {
			try {
				parent.serverSocket = new ServerSocket(tcpPort);
				while (true) { // Wait for the clients
					System.out.println(".......... serverSocket waiting (TCP:" + tcpPort + ").");
					Socket clientSkt = serverSocket.accept();
					System.out.println(".......... serverSocket accepted (TCP:" + tcpPort + ").");
					parent.setSocket(clientSkt);
				}
			} catch (Exception ex) {
				System.err.println("SocketThread:" + ex.getLocalizedMessage());
			}
			System.out.println("..... End of TCP SocketThread.");
		}
	}

	public void createSocketThread() {
		try {
			SocketThread socketThread = new SocketThread(this);
			socketThread.start();
		} catch (Exception ex) {
			throw ex;
		}
	}

	protected void setSocket(Socket skt) {
		this.clientSocketlist.add(skt);
	}

	public void write(byte[] message) {
		List<Socket> toRemove = new ArrayList<>();
		synchronized( clientSocketlist) {
			clientSocketlist.stream().forEach(tcpSocket -> { // TODO Synchronize the stream itself?
				synchronized (tcpSocket) {
					try {
						DataOutputStream out = null;
						if (out == null) {
							out = new DataOutputStream(tcpSocket.getOutputStream());
						}
						out.write(message);
						out.flush();
					} catch (SocketException se) {
						toRemove.add(tcpSocket);
					} catch (Exception ex) {
						System.err.println("RasPiTCPRadar.write:" + ex.getLocalizedMessage());
						ex.printStackTrace();
					}
				}
			});
		}

		if (toRemove.size() > 0) {
			synchronized (clientSocketlist) {
				toRemove.stream().forEach(this.clientSocketlist::remove);
			}
		}
	}

	private int getNbClients() {
		return clientSocketlist.size();
	}

	private String formatByteHexa(byte b) {
		String s = Integer.toHexString(b).toUpperCase();
		while (s.length() < 2) {
			s = "0" + s;
		}
		return s;
	}

	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			for (Socket tcpSocket : clientSocketlist) {
				tcpSocket.close();
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void main(String... args) {

		Consumer<RasPiRadar.DirectionAndRange> defaultDataConsumer = (data) -> {
			buffer.add(data.range());
			while (buffer.size() > BUFFER_LENGTH) {
				buffer.remove(0);
			}
			double avg = buffer.stream().mapToDouble(d -> d).average().getAsDouble();
			System.out.println(String.format("Default (static) RasPiRadar Consumer >> Bearing %s%02d, distance %.02f cm", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), avg));
		};

		int servoPort  = 0;

		Integer trig = null, echo = null;

		// User prms
		for (String str : args) {
			if (str.startsWith(PCA9685_SERVO_PORT)) {
				String s = str.substring(PCA9685_SERVO_PORT.length());
				servoPort = Integer.parseInt(s);
			}
			if (str.startsWith(DELAY)) {
				String s = str.substring(DELAY.length());
				delay = Long.parseLong(s);
			}
			// Trig & Echo pin PHYSICAL numbers (1..40)
			if (str.startsWith(TRIGGER_PIN)) {
				String s = str.substring(TRIGGER_PIN.length());
				trig = Integer.parseInt(s);
			}
			if (str.startsWith(ECHO_PIN)) {
				String s = str.substring(ECHO_PIN.length());
				echo = Integer.parseInt(s);
			}
			if (str.equals(JUST_RESET)) {
				justReset = true;
			}
			if (str.equals(JUST_ONE_LOOP)) {
				justOneLoop = true;
			}
		}
		if (echo != null ^ trig != null) {
			throw new RuntimeException("Echo & Trigger pin numbers must be provided together, or not at all.");
		}

		System.out.println(String.format("Driving Servo on Channel %d", servoPort));
		System.out.println(String.format("Wait when scanning %d ms", delay));

		RasPiRadar rpr = null;
		try {
			if (echo == null && trig == null) {
				rpr = new RasPiRadar(true, servoPort);
			} else {
				rpr = new RasPiRadar(true, servoPort, PinUtil.getPinByPhysicalNumber(trig), PinUtil.getPinByPhysicalNumber(echo));
			}
		} catch (I2CFactory.UnsupportedBusNumberException | UnsatisfiedLinkError notOnAPi) {
			System.out.println("Not on a Pi? Moving on...");
		}

		if (rpr != null && rpr.getHcSR04() != null && verbose) {
			System.out.println("HC-SR04 wiring:");
			String[] map = new String[2];
			map[0] = String.valueOf(PinUtil.findByPin(rpr.getHcSR04().getTrigPin()).pinNumber()) + ":" + "Trigger";
			map[1] = String.valueOf(PinUtil.findByPin(rpr.getHcSR04().getEchoPin()).pinNumber()) + ":" + "Echo";

			PinUtil.print(map);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> loop = false, "Shutdown Hook"));

		RasPiTCPRadar radar = new RasPiTCPRadar();
		radar.tcpPort = Integer.parseInt(System.getProperty("tcp.port", String.valueOf(radar.tcpPort)));

		radar.createSocketThread();

		rpr.setDataConsumer(data -> {
			buffer.add(data.range());
			while (buffer.size() > BUFFER_LENGTH) {
				buffer.remove(0);
			}
			double avg = buffer.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
			String serialSentence = String.format("%s%02d;%.02f\t", (data.direction < 0 ? "-" : "+"), Math.abs(data.direction), avg);
			if (verbose) {
				System.out.println(String.format("Emitting [%s]", serialSentence));
			}
			radar.write(serialSentence.getBytes());
		});
		// For simulation, override if needed
//	rpr.setRangeSimulator(RasPiRadar::simulateUserRange);

		try {
			if (rpr != null) {
				rpr.stop(); // init
			}
			if (justReset) {
				loop = false;
				TimeUtil.delay(1_000L);
			}

			int inc = 1;
			int bearing = 0;
			double dist = 0;
			int hitExtremity = 0;
			while (loop) {
				try {
					if (rpr != null) {
						rpr.setAngle(bearing);
						// Measure distance here, broadcast it witdouble dist = h bearing.
						dist = rpr.readDistance();
						// Consumer
						rpr.consumeData(new RasPiRadar.DirectionAndRange(bearing, dist));
					} else { // For dev...
						defaultDataConsumer.accept(new RasPiRadar.DirectionAndRange(bearing, dist));
					}
					if (justOneLoop && hitExtremity == 2 && bearing == 0) {
						loop = false;
					}

					bearing += inc;
					if (bearing > 90 || bearing < -90) { // then flip
						hitExtremity += 1;
						inc *= -1;
						bearing += (2 * inc);
					}
					// Sleep here
					TimeUtil.delay(delay);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} finally {
			if (rpr != null) {
				rpr.stop();
				TimeUtil.delay(1_000L); // Before freeing, get some time to get back to zero.
				rpr.free();
			}
			radar.close();
		}
		System.out.println("Done.");
	}
}
