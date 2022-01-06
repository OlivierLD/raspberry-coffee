package tcptests;


import nmea.ais.AISParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Date;

public class GPSDClient {
	private int tcpPort = 80;
	private String hostName = "localhost";
	private final AISParser aisParser = new AISParser();

	private boolean goRead = true;

	public GPSDClient() {
	}

	public GPSDClient(int tcp) {
		tcpPort = tcp;
	}

	public GPSDClient(String host, int tcp) {
		hostName = host;
		tcpPort = tcp;
	}

	private Socket skt = null;

	private boolean canRead() {
		return this.goRead;
	}
	private void stopReading() {
		this.goRead = false;
	}

	public void read() {
		read(null);
	}

	public void read(String request) {
		boolean verbose = "true".equals((System.getProperty("verbose", "false")));
		System.out.println("From " + getClass().getName() + " Reading TCP Port " + tcpPort + " on " + hostName);
		try {
			InetAddress address = InetAddress.getByName(hostName);
//    System.out.println("INFO:" + hostName + " (" + address.toString() + ")" + " is" + (address.isMulticastAddress() ? "" : " NOT") + " a multicast address");
			skt = new Socket(address, tcpPort);

			if (request != null) {
				OutputStream os = skt.getOutputStream();
				if (false) {
					DataOutputStream out = new DataOutputStream(os);
					out.writeBytes(request + "\n"); // LF is the end of message!!!
					out.flush();
				} else {
					PrintWriter out = new PrintWriter(os, true);
					out.println(request);
				}
			}

			InputStream theInput = skt.getInputStream();
			byte[] buffer = new byte[4_096];
			int nbReadTest = 0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (canRead()) {
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
					if (buffer[bytesRead -1] == '\n') {
						String message = baos.toString().trim();
						// Manage message here
//						DumpUtil.displayDualDump(message);
						if (verbose) {
							System.out.println("---- GPSD Mess ----");
							System.out.println(message);
							System.out.println("-------------------");
						}
						String[] split = message.split("\n");
						Arrays.asList(split)
								.stream()
								.forEach(mess -> {
									if (mess.startsWith("!")) {
										try {
											AISParser.AISRecord aisRecord = aisParser.parseAIS(mess);
											System.out.println(String.format("Parsed: %s", aisRecord.toString()));
										} catch (AISParser.AISException aisEx) {
											System.err.println(aisEx.toString());
										} catch (Exception ex) {
											System.err.println(" Oops >> " + ex.toString());
										}
									} else if (mess.startsWith("$")) {
										System.out.println(">> NMEA Stuff ? >> " + mess);
									} else {
										if (verbose) {
											System.out.println(">> GPSD Mess >> " + mess);
										}
									}
								});
					}
				}
			}
			System.out.println("Stop Reading TCP port.");
			theInput.close();
			// Signal waiter
			if (waiter != null) {
				synchronized (waiter) {
					waiter.notify();
				}
			}
			System.out.println("Tchao!");
		} catch (BindException be) {
			System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + tcpPort);
			be.printStackTrace();
			manageError(be);
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
						closeReader();
						Thread.sleep(1_000L);
						System.out.println("Re-trying now. (from " + this.getClass().getName() + ")");
						read();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else
					manageError(se);
			}
		} catch (Exception e) {
//    e.printStackTrace();
			manageError(e);
		}
	}

	public void closeReader() throws Exception {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
		try {
			if (skt != null) {
				this.goRead = false;
				skt.close();
				skt = null;
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	public void manageError(Throwable t) {
		throw new RuntimeException(t);
	}

	public void setTimeout(long timeout) { /* Not used for TCP */ }

	private static Thread waiter = null;

	public static void main(String... args) {

		waiter = Thread.currentThread();

//		System.setProperty("verbose", "true");
		String host = "sinagot.net"; // "localhost";
		int port = 2947;
		try {
			boolean keepTrying = true;
			while (keepTrying) {
				final GPSDClient tcpClient = new GPSDClient(host, port);
				System.out.println(new Date().toString() + ": New " + tcpClient.getClass().getName() + " created.");

				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					if (tcpClient != null) {
						System.out.println("\n>> Stop reading");
						tcpClient.stopReading();
						synchronized (waiter) {
							try {
								waiter.wait();
								System.out.println("Bye-bye.");
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
				}, "Hook"));

				try {
					// Initiate. Request
					tcpClient.read("?WATCH={\"enable\":true,\"json\":false,\"nmea\":true,\"raw\":0,\"scaled\":false,\"timing\":false,\"split24\":false,\"pps\":false}");
				} catch (Exception ex) {
					System.err.println("TCP Reader:" + ex.getMessage());
					ex.printStackTrace();

					tcpClient.closeReader();
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
