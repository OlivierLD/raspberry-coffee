package nmea.consumers.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAParser;
import nmea.api.NMEAReader;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.List;

public class UDPReader extends NMEAReader {
	private final static String DEFAULT_HOST_NAME = "localhost";
	private final static int DEFAULT_UDP_PORT = 8001;
	private String host = DEFAULT_HOST_NAME;
	private int udpPort = DEFAULT_UDP_PORT;
	private long timeout = 5_000L; // Default value

	public UDPReader(List<NMEAListener> al) {
		this(null, al, DEFAULT_HOST_NAME, DEFAULT_UDP_PORT);
	}

	public UDPReader(List<NMEAListener> al, int udp) {
		this(null, al, DEFAULT_HOST_NAME, udp);
	}

	public UDPReader(List<NMEAListener> al, String host, int udp) {
		this(null, al, host, udp);
	}
	public UDPReader(String threadName, List<NMEAListener> al, String host, int udp) {
		super(threadName, al);
		udpPort = udp;
		this.host = host;
	}

	private InetAddress group = null;
	private DatagramSocket dsocket = null;

	public int getPort() {
		return this.udpPort;
	}

	public String getHostname() {
		return this.host;
	}

	@Override
	public void startReader() {
		System.out.println("From " + getClass().getName() + " Reading UDP Port " + udpPort);
		super.enableReading();
		try {
			InetAddress address = InetAddress.getByName(host);
			if (address.isMulticastAddress()) {
				dsocket = new MulticastSocket(udpPort);
				((MulticastSocket) dsocket).joinGroup(address);
				group = address;
			} else {
				dsocket = new DatagramSocket(udpPort, address);
			}

			byte buffer[] = new byte[4096];
			String s;
			while (this.canRead()) {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				// Wait here.
				Thread waiter = Thread.currentThread();
				DatagramReceiveThread drt = new DatagramReceiveThread(dsocket, packet, waiter);
				drt.start();

				synchronized (waiter) {
					try {
						long before = System.currentTimeMillis();
						if (timeout > -1)
							waiter.wait(timeout);
						else
							waiter.wait();
						long after = System.currentTimeMillis();
						if (drt.isAlive()) {
//            System.out.println("Interrupting the DatagramReceiveThread");
							drt.interrupt();
							if (timeout != -1 && (after - before) >= timeout)
								throw new RuntimeException("UDP took too long.");
						}
					} catch (InterruptedException ie) {
						System.out.println("Waiter Interrupted! (before end of wait, good)");
					}
				}
				s = new String(buffer, 0, packet.getLength());
				// For simulation from file:
				if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
					if (s.endsWith(NMEAParser.NMEA_SENTENCE_SEPARATOR))
						s = s.substring(0, s.length() - NMEAParser.NMEA_SENTENCE_SEPARATOR.length());
				}
				if (!s.endsWith(NMEAParser.NMEA_SENTENCE_SEPARATOR)) {
					s += NMEAParser.NMEA_SENTENCE_SEPARATOR;
				}
				NMEAEvent n = new NMEAEvent(this, s);
				super.fireDataRead(n);
			}
		} catch (Exception e) {
//    e.printStackTrace();
//    JOptionPane.showMessageDialog(null, "No such UDP port " + udpport + "!", "Error opening port", JOptionPane.ERROR_MESSAGE);
			manageError(e);
		} finally {
			try {
				if (dsocket != null) {
					if (dsocket instanceof MulticastSocket) {
						((MulticastSocket) dsocket).leaveGroup(group);
					}
					dsocket.close();
				}
			} catch (Exception ex) {
				System.err.println(">> Error when Closing Socket...");
				ex.printStackTrace();
			}
//    closeReader();
		}
	}

	@Override
	public void closeReader() throws Exception {
//  System.out.println("(" + this.getClass().getName() + ") Stop Reading UDP Port");
		try {
			if (dsocket != null) {
				this.goRead = false;
				if (dsocket instanceof MulticastSocket) {
					if (group != null)
						((MulticastSocket) dsocket).leaveGroup(group);
					else
						System.out.println(">> Multicast Socket: Group is null.");
				}
				dsocket.close();
				dsocket = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void manageError(Throwable t) {
		throw new RuntimeException(t);
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getTimeout() {
		return timeout;
	}

	private class DatagramReceiveThread extends Thread {
		private DatagramSocket ds = null;
		private Thread waiter;
		private DatagramPacket packet;

		public DatagramReceiveThread(DatagramSocket ds, DatagramPacket packet, Thread from) {
			super();
			this.ds = ds;
			this.waiter = from;
			this.packet = packet;
		}

		public void run() {
			try {
//      dsocket.receive(packet);
				ds.receive(packet);
				synchronized (waiter) {
//        System.out.println("Notifying waiter (Done).");
					waiter.notify();
				}
			} catch (SocketException se) {
				// Socket closed?
				if (!"Socket closed".equals(se.getMessage()))
					System.out.println(">>>>> " + this.getClass().getName() + ":" + se.getMessage());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
