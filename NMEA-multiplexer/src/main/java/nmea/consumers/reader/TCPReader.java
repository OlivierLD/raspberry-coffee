package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;

import java.io.InputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TCP reader
 */
public class TCPReader extends NMEAReader {
	private final static String DEFAULT_HOST_NAME = "localhost";
	private final static int DEFAULT_TCP_PORT = 80;
	private int tcpPort = DEFAULT_TCP_PORT;
	private String hostName = DEFAULT_HOST_NAME;

	public TCPReader(List<NMEAListener> al) {
		this(null, al, DEFAULT_HOST_NAME, DEFAULT_TCP_PORT);
	}

	public TCPReader(List<NMEAListener> al, int tcp) {
		this(null, al, DEFAULT_HOST_NAME, tcp);
	}

	public TCPReader(List<NMEAListener> al, String host, int tcp) {
		this(null, al, host, tcp);
	}
	public TCPReader(String threadName, List<NMEAListener> al, String host, int tcp) {
		super(threadName, al);
		hostName = host;
		tcpPort = tcp;
	}

	private Socket skt = null;

	public int getPort() {
		return this.tcpPort;
	}

	public String getHostname() {
		return this.hostName;
	}

	@Override
	public void startReader() {
		super.enableReading();
		try {
			InetAddress address = InetAddress.getByName(hostName);
//    System.out.println("INFO:" + hostName + " (" + address.toString() + ")" + " is" + (address.isMulticastAddress() ? "" : " NOT") + " a multicast address");
			skt = new Socket(address, tcpPort);

			InputStream theInput = skt.getInputStream();
			byte buffer[] = new byte[4096];
			String s;
			int nbReadTest = 0;
			while (this.canRead()) {
				int bytesRead = theInput.read(buffer);
				if (bytesRead == -1) {
					System.out.println("Nothing to read...");
					if (nbReadTest++ > 10)
						break;
				} else {
					int nn = bytesRead;
					for (int i = 0; i < Math.min(buffer.length, bytesRead); i++) {
						if (buffer[i] != 0)
							continue;
						nn = i;
						break;
					}

					byte toPrint[] = new byte[nn];
					for (int i = 0; i < nn; i++) {
						toPrint[i] = buffer[i];
					}

					s = new String(toPrint) + NMEAParser.NMEA_SENTENCE_SEPARATOR;
	//      System.out.println("TCP:" + s);
					NMEAEvent n = new NMEAEvent(this, s);
					super.fireDataRead(n);
				}
			}
			System.out.println("Stop Reading TCP port.");
			theInput.close();
		} catch (BindException be) {
			System.err.println("From " + this.getClass().getName() + ", " + hostName + ":" + tcpPort);
			be.printStackTrace();
			manageError(be);
		} catch (final SocketException se) {
//			if ("true".equals(System.getProperty("tcp.data.verbose"))) {
//		    se.printStackTrace();
//			}
			if (se.getMessage().indexOf("Connection refused") > -1) {
				System.out.println("Refused (1)");
			} else if (se.getMessage().indexOf("Connection reset") > -1) {
				System.out.println("Reset (2)");
			} else {
				boolean tryAgain = false;
				if (se instanceof ConnectException && "Connection timed out: connect".equals(se.getMessage())) {
					if ("true".equals(System.getProperty("tcp.data.verbose"))) {
						System.out.println("Will try again (1)");
					}
					tryAgain = true;
					if ("true".equals(System.getProperty("tcp.data.verbose"))) {
						System.out.println("Will try again (2)");
					}
				} else if (se instanceof SocketException && se.getMessage().startsWith("Network is unreachable (connect ")) {
					if ("true".equals(System.getProperty("tcp.data.verbose"))) {
						System.out.println("Will try again (3)");
					}
					tryAgain = true;
				} else if (se instanceof ConnectException) { // Et hop!
					tryAgain = false;
					System.err.println("TCP :" + se.getMessage());
				} else {
					tryAgain = false;
					System.err.println("TCP Socket:" + se.getMessage());
				}
			}
		} catch (Exception e) {
//    e.printStackTrace();
			manageError(e);
		}
	}

	@Override
	public void closeReader() throws Exception {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading TCP Port");
		try {
			if (skt != null) {
				this.goRead = false;
				skt.close();
				skt = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void manageError(Throwable t) {
		throw new RuntimeException(t);
	}

	public void setTimeout(long timeout) { /* Not used for TCP */ }

	/**
	 * For tests
	 * @param args Unused
	 */
	public static void main(String... args) {
		/*
		-Dtcp.data.verbose=true
		-Dtcp.proxyHost=www-proxy.us.oracle.com
		-Dtcp.proxyPort=80
		-Dhttp.proxyHost=www-proxy.us.oracle.com
		-Dhttp.proxyPort=80
		-DsocksProxyHost=www-proxy.us.oracle.com
		-DsocksProxyPort=80
		 */

		String host = // "192.168.42.2";
									"ais.exploratorium.edu";
		int port = 80; // 7001; // 2947
		try {
			List<NMEAListener> ll = new ArrayList<>();
			NMEAListener nl = new NMEAListener() {
				@Override
				public void dataRead(NMEAEvent nmeaEvent) {
					System.out.println(nmeaEvent.getContent()); // TODO Send to a GUI?
				}
			};
			ll.add(nl);

			boolean keepTrying = true;
			while (keepTrying) {
				TCPReader ctcpr = new TCPReader(ll, host, port);
				System.out.println(new Date().toString() + ": New " + ctcpr.getClass().getName() + " created.");

				try {
					ctcpr.startReader();
				} catch (Exception ex) {
					System.err.println("TCP Reader:" + ex.getMessage());
					ctcpr.closeReader();
					long howMuch = 1_000L;
					System.out.println("Will try to reconnect in " + Long.toString(howMuch) + "ms.");
					try {
						Thread.sleep(howMuch);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
