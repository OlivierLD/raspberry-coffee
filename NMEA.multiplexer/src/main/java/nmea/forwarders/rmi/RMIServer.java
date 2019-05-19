package nmea.forwarders.rmi;

import context.ApplicationContext;
import nmea.forwarders.Forwarder;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RMIServer extends UnicastRemoteObject implements ServerInterface, Forwarder {

	private static final String DEFAULT_NAME = "RMIServer";
	private Registry registry     = null;
	private int registryPort      = 1099;
	private String serverAddress  = "";
	private String bindingName    = DEFAULT_NAME;
	private Properties props = null;

	private boolean verbose = false;

	private final static String ipPattern = "(?:(?:1\\d?\\d|[1-9]?\\d|2[0-4]\\d|25[0-5])\\.){3}(?:1\\d?\\d|[1-9]?\\d|2[0-4]\\d|25[0-\u200C\u200B5])(?:[:]\\d+)?";

	public RMIServer(int port) throws RemoteException {
		this(port, DEFAULT_NAME);
	}

	public RMIServer(int port, String bindingName) throws RemoteException {
		// Make sure the cache has been initialized.
		if (ApplicationContext.getInstance().getDataCache() == null) {
			throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
		}

		this.registryPort = port;
		this.bindingName = bindingName;

		Pattern pattern = Pattern.compile(ipPattern);

		try {
//		this.serverAddress = (InetAddress.getLocalHost()).toString();
			String address = "";
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			while (n.hasMoreElements()) {
				NetworkInterface e = n.nextElement();

				Enumeration<InetAddress> a = e.getInetAddresses();
				while (a.hasMoreElements()) {
					InetAddress addr = a.nextElement();
					String hostAddress = addr.getHostAddress();
					if (!"127.0.0.1".equals(hostAddress)) {
						Matcher matcher = pattern.matcher(hostAddress);
						if (matcher.matches()) {
							address = hostAddress;
							break;
						}
					}
				}
				if (address.trim().length() > 0) {
					break;
				}
			}

			this.serverAddress = address; // InetAddress.getLocalHost().getHostAddress();
			System.setProperty("java.rmi.server.hostname", this.serverAddress);
		} catch(Exception e) {
			throw new RemoteException("Can't get inet address.");
		}
		try{
			registry = LocateRegistry.createRegistry(registryPort);
			registry.rebind(bindingName, this);
			System.out.println(String.format("RMI Server Created. Server address : %s, port %d, name %s", this.serverAddress, this.registryPort, this.bindingName));
		} catch (RemoteException e) {
			throw e;
		}
	}

	public int getRegistryPort() {
		return this.registryPort;
	}
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Receives a message from the MUX
	 *
	 * @param message NMEA String
	 */
	@Override
	public void write(byte[] message) {
		// Feed the cache here
//	ApplicationContext.getInstance().getDataCache().parseAndFeed(new String(message)); // Redundant with the MUX.
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
			this.registry.unbind(this.bindingName);
			UnicastRemoteObject.unexportObject(this, true);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		System.out.println(" >> RMI server shutdown.");
	}

	@Override
	public <T> T executeTask(Task<T> t) throws RemoteException {
		System.out.println(String.format(">> Server task [%s] requested.", t.getClass().getName()));
		return t.execute();
	}

	public static class RMIBean {
		private String cls;
		private int port;
		private String type = "rmi";
		private String bindingName;
		private String serverAddress;

		public int getPort() {
			return this.port;
		}
		public String getBindingName() {
			return this.bindingName;
		}

		public RMIBean(RMIServer instance) {
			this.cls = instance.getClass().getName();
			this.port = instance.registryPort;
			this.bindingName = instance.bindingName;
			this.serverAddress = instance.serverAddress;
		}
	}

	@Override
	public Object getBean() {
		return new RMIBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		this.props = props;
	}

	// For standalone tests
	public static void main_(String... args) throws RemoteException {
		RMIServer server = new RMIServer(1099);
	}

	public static void main(String... args) throws Exception {

		String ipPattern = "(?:(?:1\\d?\\d|[1-9]?\\d|2[0-4]\\d|25[0-5])\\.){3}(?:1\\d?\\d|[1-9]?\\d|2[0-4]\\d|25[0-\u200C\u200B5])(?:[:]\\d+)?";
		Pattern pattern = Pattern.compile(ipPattern);

		System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress());  // often returns "127.0.0.1"
		Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
		for (; n.hasMoreElements(); ) {
			NetworkInterface e = n.nextElement();

			Enumeration<InetAddress> a = e.getInetAddresses();
			for (; a.hasMoreElements(); ) {
				InetAddress addr = a.nextElement();
				System.out.println("  " + addr.getHostAddress());
				Matcher matcher = pattern.matcher(addr.getHostAddress());
				System.out.println("    Matches:" + matcher.matches());
			}
		}
	}
}
