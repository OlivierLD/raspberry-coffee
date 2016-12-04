package servers;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UDPWriter implements Forwarder {
	private int udpPort = 8001;
	private InetAddress address = null;

	private final static String DEFAULT_HOST = "127.0.0.1"; // "230.0.0.1"
	private String hostName = DEFAULT_HOST;

	public UDPWriter(int port) throws Exception {
		this(port, DEFAULT_HOST);
	}

	public UDPWriter(int port, String host) throws Exception {
		this.hostName = host;
		this.udpPort = port;
		try {
			address = InetAddress.getByName(this.hostName); // For Broadcasting, multicast address.
		} catch (Exception ex) {
			throw ex;
//    ex.printStackTrace();
		}
	}

	@Override
	public void write(byte[] message) {
		try {
			// Create datagram socket
			DatagramSocket dsocket = null;
			if (address.isMulticastAddress()) {
				dsocket = new MulticastSocket(udpPort);
				((MulticastSocket) dsocket).joinGroup(address);
			} else
				dsocket = new DatagramSocket(udpPort, address);

			// Initialize a datagram
			DatagramPacket packet = new DatagramPacket(message, message.length, address, udpPort);
			dsocket.send(packet);
			if (address.isMulticastAddress())
				((MulticastSocket) dsocket).leaveGroup(address);
			dsocket.close();
		} catch (Exception ex) {
			if ("No such device".equals(ex.getMessage()))
				System.out.println("No such devide [" + address + "] (from " + this.getClass().getName() + ")");
			else
				ex.printStackTrace();
		}
	}

	@Override
	public void close() {
	}

	private static class UDPBean {
		String cls;
		int port;

		public UDPBean(UDPWriter instance) {
			cls = instance.getClass().getName();
			port = instance.udpPort;
		}
	}

	@Override
	public Object getBean() {
		return new UDPBean(this);
	}

}

