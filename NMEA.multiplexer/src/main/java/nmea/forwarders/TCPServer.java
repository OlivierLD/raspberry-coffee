package nmea.forwarders;

import nmea.ais.AISParser;

import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TCPServer implements Forwarder {
	private TCPServer instance = this;
	private List<Socket> clientSocketList = new ArrayList<>(1);
	private Properties props = null;

	private int tcpPort = 7001; // Default
	private ServerSocket serverSocket = null;

	private boolean withAIS = true;

	public TCPServer(int port) throws Exception {
		this.tcpPort = port;

		try {
			SocketThread socketThread = new SocketThread(this);
			socketThread.start();
		} catch (Exception ex) {
			throw ex;
		}
	}

	public int getTcpPort() {
		return this.tcpPort;
	}

	protected void setSocket(Socket skt) {
		this.clientSocketList.add(skt);
	}

	@Override
	public void write(byte[] message) {
		if (!withAIS) {
			if (new String(message).trim().startsWith(AISParser.AIS_PREFIX)) {
//				System.out.println("\t\t>> From TCP Skipping AIS message");
				return;
			}
		}

		List<Socket> toRemove = new ArrayList<>();
		synchronized(clientSocketList) {
			clientSocketList.stream().forEach(tcpSocket -> { // TODO Synchronize the stream?
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
						System.err.println("TCPWriter.write:" + ex.getLocalizedMessage());
						ex.printStackTrace();
					}
				}
			});
		}

		if (toRemove.size() > 0) {
			synchronized (clientSocketList) {
				toRemove.stream().forEach(this.clientSocketList::remove);
			}
		}
	}

	private int getNbClients() {
		return clientSocketList.size();
	}

	private String formatByteHexa(byte b) {
		String s = Integer.toHexString(b).toUpperCase();
		while (s.length() < 2)
			s = "0" + s;
		return s;
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			for (Socket tcpSocket : clientSocketList)
				tcpSocket.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private class SocketThread extends Thread {
		private TCPServer parent = null;

		public SocketThread(TCPServer parent) {
			super("TCPServer");
			this.parent = parent;
		}

		public void run() {
			try {
				parent.serverSocket = new ServerSocket(tcpPort);
				while (true) { // Wait for the clients
					if (instance.props != null && "true".equals(instance.props.getProperty("verbose"))) {
						System.out.println(".......... serverSocket waiting (TCP:" + tcpPort + ").");
					}
					Socket clientSkt = serverSocket.accept();
					if (instance.props != null && "true".equals(instance.props.getProperty("verbose"))) {
						System.out.println(".......... serverSocket accepted (TCP:" + tcpPort + ").");
					}
					parent.setSocket(clientSkt);
				}
			} catch (Exception ex) {
				System.err.println("SocketThread:" + ex.getLocalizedMessage());
			}
			System.out.println("..... End of TCP SocketThread.");
		}
	}

	public static class TCPBean {
		private String cls;
		private int port;
		private String type = "tcp";
		private int nbClients = 0;

		public int getPort() {
			return port;
		}

		public TCPBean(TCPServer instance) {
			cls = instance.getClass().getName();
			port = instance.tcpPort;
			nbClients = instance.getNbClients();
		}
	}

	@Override
	public Object getBean() {
		return new TCPBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
		if (this.props != null) {
			this.withAIS = "true".equals(this.props.getProperty("with.ais", "true"));
//			System.out.println("\t\t>> NO AIS !!!");
		}
	}

	public static void main(String... args) {
//	String gpsd = "{\"class\":\"TVP\",\"tag\":\"MID2\",\"time\":\"2010-04-30T11:48:20.10Z\",\"ept\":0.005,\"lat\":46.498204497,\"lon\":7.568061439,\"alt\":1327.689,\"epx\":15.319,\"epy\":17.054,\"epv\":124.484,\"track\":10.3797,\"speed\":0.091,\"climb\":-0.085,\"eps\",34.11,\"mode\":3}";
		String gpsd = "?WATCH={...};";
		String wpl = "$GPWPL,3739.856,N,12222.812,W,OPMRNA*59";
		try {
			TCPServer tcpw = new TCPServer(2947); // 2947
//    TCPWriter tcpw = new TCPWriter(7001);
//    TCPWriter tcpw = new TCPWriter(7001, "theketch-lap.mshome.net");
			for (int i = 0; i < 50; i++) {
				System.out.println("Ping...");
				try {
					tcpw.write(gpsd.getBytes());
				} catch (Exception ex) {
					System.err.println(ex.getLocalizedMessage());
				}
				try {
					Thread.sleep(1_000L);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
