package nmea.forwarders.rmi;

import context.ApplicationContext;
import nmea.forwarders.Forwarder;
import nmea.forwarders.rmi.clientoperations.LastString;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements ServerInterface, Forwarder {

	private static final String DEFAULT_NAME = "RMIServer";
	private Registry registry     = null;
	private int registryPort      = 1099;
	private String serverAddress  = "";
	private String bindingName    = DEFAULT_NAME;

	private boolean verbose = false;

	public RMIServer(int port) throws RemoteException {
		this(port, DEFAULT_NAME);
	}

	public RMIServer(int port, String bindingName) throws RemoteException {
		// Make sure the cache has been initialized.
		if (ApplicationContext.getInstance().getDataCache() == null) {
			throw new RuntimeException("Init the Cache first. See the properties file used at started."); // Oops
		}

		this.registryPort = port;
		this.bindingName = bindingName;

		try {
			serverAddress = (InetAddress.getLocalHost()).toString();
		} catch(Exception e) {
			throw new RemoteException("Can't get inet address.");
		}
		try{
			registry = LocateRegistry.createRegistry(registryPort);
			registry.rebind(bindingName, this);
			System.out.println(String.format("RMI Server Created. Server address : %s, port %d, name %s", serverAddress, registryPort, bindingName));
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

	private String lastString = "";

	/**
	 * Receives a message from the MUX
	 *
	 * @param message NMEA String
	 */
	@Override
	public void write(byte[] message) {
		this.lastString = new String(message);
		// Feed the cache here
		ApplicationContext.getInstance().getDataCache().parseAndFeed(new String(message)); // TODO CHeck if this is not redundant with the MUX.
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
		if (t instanceof LastString) {
			((LastString)t).setLastString(this.lastString);
		}
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

	// For standalone tests
	public static void main(String[] args) throws RemoteException {
		RMIServer server = new RMIServer(1099);
	}
}
