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
 * Basic TCP plumbing.
 * No main here.
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
		System.out.println("From " + getClass().getName() + " Reading TCP Port " + tcpPort + " on " + hostName + ", verbose=" + verbose);
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
						int bytesRead = -1;
						try {
							bytesRead = theInput.read(buffer);
						} catch (SocketException se) {
							if (se.getMessage().startsWith("Socket closed")) {
								if (verbose) {
									System.out.println("\nSocket closed (Managed).");
								}
							} else {
								se.printStackTrace();
							}
						}
						if (bytesRead == -1) {
							if (verbose) {
								System.out.println("Nothing to read...");
							}
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
			if (verbose) {
				System.out.printf("\tStop Reading TCP port, at %s\n", SDF.format(new Date()));
			}
			theInput.close();
			if (verbose) {
				System.out.printf("\tTchao! at %s\n", SDF.format(new Date()));
			}
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
		if (verbose) {
			System.out.println(">> End of 'read()'");
		}
	}

	public void stopConnection() throws Exception {
		this.in.close();
		this.out.close();
		this.clientSocket.close();
		this.goRead = false;
	}
}
