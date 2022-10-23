package clients.tcp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

/**
 * JUST a simple TCP Client, expecting to receive NMEA Data.
 * Requires an NMEA-multiplexer to be running on TCP:7001 (See system variables verbose, tcp.host and tcp.port)
 *
 * Also see tcptests.TCPClient.java, in NMEA-multiplexer tests.
 */
public class NMEATCPClient {

	private String hostName;
	private int tcpPort;

	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;

	private Consumer<String> whatToDo; // Externalized consumer

	private boolean goRead = true;

	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");

	public void setConsumer(Consumer<String> consumer) {
		this.whatToDo = consumer;
	}

	private boolean canRead() {
		return this.goRead;
	}

	public void startConnection(String hostName, int port) throws Exception {
		this.hostName = hostName;
		this.tcpPort = port;
		InetAddress address = InetAddress.getByName(hostName);
		clientSocket = new Socket(address, port);

		//    System.out.println("INFO:" + hostName + " (" + address.toString() + ")" + " is" + (address.isMulticastAddress() ? "" : " NOT") + " a multicast address");

		out = new PrintWriter(clientSocket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	}

	public String sendMessage(String msg) throws Exception {
		out.println(msg);
		String resp = in.readLine();
		return resp;
	}

	public void read() {
		read(null);
	}

	public void read(String request) {
		boolean verbose = "true".equals((System.getProperty("verbose", "false")));
		System.out.println("From " + getClass().getName() + " Reading TCP Port " + tcpPort + " on " + hostName);
		try {
			if (request != null) {
				OutputStream os = clientSocket.getOutputStream();
				if (false) {
					DataOutputStream out = new DataOutputStream(os);
					out.writeBytes(request + "\n"); // LF is the end of message!!!
					out.flush();
				} else {
					PrintWriter out = new PrintWriter(os, true);
					out.println(request);
				}
			}

			InputStream theInput = clientSocket.getInputStream();
			byte[] buffer = new byte[4_096];
			int nbReadTest = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (canRead()) {
				// synchronized (clientSocket) {
					if (!clientSocket.isClosed()) {
						int bytesRead = theInput.read(buffer);
						if (bytesRead == -1) {
							System.out.println("Nothing to read...");
							if (nbReadTest++ > 10) {
								break;
							}
						} else {
							baos.write(buffer, 0, bytesRead);
							if (verbose) {
								System.out.println("# Read " + bytesRead + " characters");
								System.out.println("# " + (new Date()).toString());
							}
							if (buffer[bytesRead - 1] == '\n') {
								String message = baos.toString().trim();
								// Manage message here
//						DumpUtil.displayDualDump(message);
								if (verbose) {
									System.out.println("---- NMEA Mess ----");
									System.out.println(message);
									System.out.println("-------------------");
								}
								String[] split = message.split("\n"); // Separate sentences
								Arrays.asList(split)
										.stream()
										.forEach(mess -> {
											if (this.whatToDo != null) {
												this.whatToDo.accept(mess.trim());
											} else {
												System.out.println(mess.trim());
											}
										});
							}
						}
					}
				// }
			}
			System.out.printf("\tStop Reading TCP port, at %s\n", SDF.format(new Date()));
			theInput.close();
			// Signal waiter
			if (waiter != null) {
				synchronized (waiter) {
					waiter.notify();
				}
			}
			System.out.printf("\tTchao! at %s\n", SDF.format(new Date()));
		} catch (BindException be) {
			System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + tcpPort);
			be.printStackTrace();
		} catch (final SocketException se) {
//    se.printStackTrace();
			if (se.getMessage().contains("Connection refused")) {
				System.err.println("Refused (1)");
				se.printStackTrace();
			} else if (se.getMessage().contains("Connection reset")) {
				System.err.println("Reset (2)");
				se.printStackTrace();
			} else {
				boolean tryAgain;
				if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) {
					tryAgain = true;
				} else if (se instanceof ConnectException && "Network is unreachable: connect".equals(se.getMessage())) {
					tryAgain = true;
				} else if (se instanceof ConnectException) { // Et hop!
					tryAgain = false;
					System.err.println("TCP :" + se.getMessage());
					se.printStackTrace();
				} else {
					tryAgain = false;
					System.err.println("TCP Socket:" + se.getMessage());
					se.printStackTrace();
				}

				if (tryAgain) {
					// Wait and try again
					try {
						System.out.println("Timeout on TCP. Will Re-try to connect in 1s");
						try {
							if (this.clientSocket != null) {
								this.goRead = false;
								this.clientSocket.close();
								this.clientSocket = null;
							}
						} catch (Exception ex) {
							throw ex;
						}
						Thread.sleep(1_000L);
						System.out.println("Re-trying now. (from " + this.getClass().getName() + ")");
						read();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					se.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stopConnection() throws Exception {
		in.close();
		out.close();
		clientSocket.close();
	}

	private static Thread waiter = null;

	public static void main(String... args) {

		System.out.println("Hit [Ctrl + C] to stop.");

		waiter = Thread.currentThread();

		try {
			boolean keepTrying = true;
			while (keepTrying) {
				final NMEATCPClient tcpClient = new NMEATCPClient();
				// tcpClient.setConsumer(System.out::println); // Raw output
				Consumer<String> nmeaConsumer = nmea -> System.out.printf("Received: [%s]\n", nmea);
				tcpClient.setConsumer(nmeaConsumer);

				try {
					tcpClient.startConnection(
							System.getProperty("tcp.host", "localhost"),
							Integer.parseInt(System.getProperty("tcp.port", String.valueOf(7001)))
					);
				} catch (Exception ex) {
					// Ooch!
					ex.printStackTrace();
					System.exit(1);
				}
				System.out.println(new Date().toString() + ": New " + tcpClient.getClass().getName() + " created.");

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					if (tcpClient != null) {
						System.out.printf("\n\t>> Stop reading requested, at %s\n.", SDF.format(new Date()));
						try {
							tcpClient.stopConnection();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						synchronized (waiter) {
							try {
								waiter.wait();
								System.out.printf("\tBye-bye. At %s\n", SDF.format(new Date()));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}, "Hook"));

				try {
					// Initiate. Request data
					tcpClient.read();
				} catch (Exception ex) {
					System.err.println("TCP Reader:" + ex.getMessage());
					ex.printStackTrace();

					long howMuch = 1_000L;
					System.out.println("Will try to reconnect in " + howMuch + "ms.");
					try {
						Thread.sleep(howMuch);
					} catch (InterruptedException ignored) {
						// Bam!
					}
				}
				keepTrying = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
