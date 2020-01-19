package nmea.forwarders;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import utils.DumpUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GPSdServer implements Forwarder {
	private GPSdServer instance = this;
	private List<Socket> clientSocketlist = new ArrayList<>(1);
	private Properties props = null;

	private int tcpPort = 2947;
	private ServerSocket serverSocket = null;

	public GPSdServer(int port) throws Exception {
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
		this.clientSocketlist.add(skt);
	}

	/*
	 * Message from the mux
	 * @param message
	 */
	@Override
	public void write(byte[] message) {
		List<Socket> toRemove = new ArrayList<Socket>();
		synchronized( clientSocketlist) {
			clientSocketlist.stream().forEach(tcpClientSocket -> {
				synchronized (tcpClientSocket) {
					try {

						DataOutputStream out = new DataOutputStream(tcpClientSocket.getOutputStream());
						out.write(message);
						out.flush();
					} catch (SocketException se) {
						toRemove.add(tcpClientSocket);
					} catch (Exception ex) {
						System.err.println("GPSdWriter.write:" + ex.getLocalizedMessage());
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
		while (s.length() < 2)
			s = "0" + s;
		return s;
	}

	@Override
	public void close() {
		System.out.println("- Stop writing to " + this.getClass().getName());
		try {
			for (Socket tcpSocket : clientSocketlist)
				tcpSocket.close();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void main(String... args) {
//	String gpsd = "{\"class\":\"TVP\",\"tag\":\"MID2\",\"time\":\"2010-04-30T11:48:20.10Z\",\"ept\":0.005,\"lat\":46.498204497,\"lon\":7.568061439,\"alt\":1327.689,\"epx\":15.319,\"epy\":17.054,\"epv\":124.484,\"track\":10.3797,\"speed\":0.091,\"climb\":-0.085,\"eps\",34.11,\"mode\":3}";
		String gpsd = "?WATCH={...};";
		String wpl = "$GPWPL,3739.856,N,12222.812,W,OPMRNA*59";
		try {
			GPSdServer tcpw = new GPSdServer(2947); // 2947
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

	private class SocketThread extends Thread {
		private GPSdServer parent = null;

		public SocketThread(GPSdServer parent) {
			super("GPSdServer");
			this.parent = parent;
		}

		public void run() {
			try {
				parent.serverSocket = new ServerSocket(tcpPort);
				while (true) { // Wait for the clients
					if (instance.props != null && "true".equals(instance.props.getProperty("verbose"))) {
						System.out.println(".......... serverSocket waiting (GPSd:" + tcpPort + ").");
					}
					Socket clientSkt = serverSocket.accept();
					if (instance.props != null && "true".equals(instance.props.getProperty("verbose"))) {
						System.out.println(".......... serverSocket accepted (GPSd:" + tcpPort + ").");
					}
					parent.setSocket(clientSkt);

					DataOutputStream out = new DataOutputStream(clientSkt.getOutputStream());

					String clientRequest = readInputStream(clientSkt.getInputStream());
					System.out.println("Received: " + clientRequest);
					DumpUtil.displayDualDump(clientRequest);

					String requestResponse = clientRequest.toUpperCase() + '\n'; // Returns the incoming message, in upper case.
					if (clientRequest.startsWith("?WATCH={")) { // && clientRequest.endsWith("};")) {
						String json = clientRequest.substring("?WATCH=".length());
//					System.out.println(">>> GPSd WATCH request:" + json);
						Gson gson = new GsonBuilder().create();
						Map<String, Object> obj = gson.fromJson(json, Map.class);
						Object o = obj.get("nmea");
						if (o != null && o instanceof Boolean) {
							System.out.println(">>>> GPSd Activate:" + ((Boolean)o).booleanValue());
							// TODO: if false, there is a problem... not supported yet! (see the write method)
						}
//					requestResponse = "{\"class\":\"SKY\",\"device\":\"/dev/pts/1\",\"time\":\"2005-07-08T11:28:07.114Z\",\"xdop\":1.55,\"hdop\":1.24,\"pdop\":1.99,\"satellites\":[{\"PRN\":23,\"el\":6,\"az\":84,\"ss\":0,\"used\":false},{\"PRN\":28,\"el\":7,\"az\":160,\"ss\":0,\"used\":false},{\"PRN\":8,\"el\":66,\"az\":189,\"ss\":44,\"used\":true},{\"PRN\":29,\"el\":13,\"az\":273,\"ss\":0,\"used\":false},{\"PRN\":10,\"el\":51,\"az\":304,\"ss\":29,\"used\":true},{\"PRN\":4,\"el\":15,\"az\":199,\"ss\":36,\"used\":true},{\"PRN\":2,\"el\":34,\"az\":241,\"ss\":43,\"used\":true},{\"PRN\":27,\"el\":71,\"az\":76,\"ss\":43,\"used\":true}]}" + "\n";
					}
//				out.writeBytes(requestResponse);
//				clientSkt.close();
				}
			} catch (Exception ex) {
				System.err.println("SocketThread:" + ex.getLocalizedMessage());
			}
			System.out.println("..... End of GPSd SocketThread.");
		}
	}

	private String readInputStream(InputStream is) {
		String read = "";
		InputStreamReader in = new InputStreamReader(is);
		int b;
		StringBuffer sb = new StringBuffer();
		boolean keepReading = true;
		boolean top = true;
		while (keepReading) {
			if (top) { // Ugly!! Argh! :(
				try {
					Thread.sleep(100L);
				} catch (InterruptedException ie) {
				}
				top = false;
			}
			try {
				if (in.ready()) {
					b = in.read();
				} else {
					b = -1;
				}
			} catch (IOException ioe) {
				b = -1;
			}
			if (b == -1) {
				keepReading = false;
			} else {
					sb.append((char)b);
//				System.out.println("======================");
//				DumpUtil.displayDualDump(sb.toString());
//				System.out.println("======================");
			}
		}
		read = sb.toString();
		return read;
	}

	public static class GPSdBean {
		private String cls;
		private int port;
		private String type = "gpsd";
		private int nbClients = 0;

		public int getPort() {
			return port;
		}

		public GPSdBean(GPSdServer instance) {
			cls = instance.getClass().getName();
			port = instance.tcpPort;
			nbClients = instance.getNbClients();
		}
	}

	@Override
	public Object getBean() {
		return new GPSdBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}
}
