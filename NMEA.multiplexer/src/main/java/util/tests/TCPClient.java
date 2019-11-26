package util.tests;


import utils.DumpUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;

import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import java.net.SocketException;

import java.util.Date;

public class TCPClient {
	private int tcpPort = 80;
	private String hostName = "localhost";

	private boolean goRead = true;

	public TCPClient() {
	}

	public TCPClient(int tcp) {
		tcpPort = tcp;
	}

	public TCPClient(String host, int tcp) {
		hostName = host;
		tcpPort = tcp;
	}

	private Socket skt = null;

	private boolean canRead() {
		return this.goRead;
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
				DataOutputStream out = new DataOutputStream(os);
				out.writeBytes(request + "\n"); // LF is the end of message!!!
				out.flush();
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
						DumpUtil.displayDualDump(message);
						// Manage message here

					}
				}
			}
			System.out.println("Stop Reading TCP port.");
			theInput.close();
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

	public static void main(String... args) {
		System.setProperty("verbose", "true");
		String host = "localhost";
		int port = 2947;
		try {
			boolean keepTrying = true;
			while (keepTrying) {
				TCPClient tcpClient = new TCPClient(host, port);
				System.out.println(new Date().toString() + ": New " + tcpClient.getClass().getName() + " created.");

				try {
					tcpClient.read("Yo!");

					tcpClient.read("?WATCH={...};");

					tcpClient.read("exit");

				} catch (Exception ex) {
					System.err.println("TCP Reader:" + ex.getMessage());
					ex.printStackTrace();

					tcpClient.closeReader();
					long howMuch = 1_000L;
					System.out.println("Will try to reconnect in " + howMuch + "ms.");
					try {
						Thread.sleep(howMuch);
					} catch (InterruptedException ignored) {
					}
				}
				keepTrying = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
