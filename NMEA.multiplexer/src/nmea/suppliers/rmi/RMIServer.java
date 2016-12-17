package nmea.suppliers.rmi;

import nmea.suppliers.Forwarder;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMIServer extends UnicastRemoteObject implements ServerInterface, Forwarder {

	private static final String DEFAULT_NAME = "RMIServer";
	private Registry registry     = null;
	private int registryPort      = 1099;
	private String serverAddress = "";
	private String bindingName = DEFAULT_NAME;

	public RMIServer(int port) throws RemoteException {
		this(port, DEFAULT_NAME);
	}
	public RMIServer(int port, String bindingName) throws RemoteException {
		this.registryPort = port;
		this.bindingName = bindingName;

		try {
			serverAddress = (InetAddress.getLocalHost()).toString();
		} catch(Exception e) {
			throw new RemoteException("Can't get inet address.");
		}
		System.out.println(String.format("RMI Server Created. Server address : %s, port %d, name %s", serverAddress, registryPort, bindingName));
		try{
			registry = LocateRegistry.createRegistry(registryPort);
			registry.rebind(bindingName, this);
		} catch (RemoteException e) {
			throw e;
		}
	}

	public int getRegistryPort() {
		return this.registryPort;
	}

	/** Receives a message from the MUX
	 *
	 * @param message NMEA String
	 */
	@Override
	public void write(byte[] message) {
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
	}

	@Override
	public <T> T executeTask(Task<T> t) throws RemoteException {
		System.out.println("Server task requested.");
		return t.execute();
	}

	public static class RMIBean {
		private String cls;
		private int port;
		private String type = "rmi";
		private String bindingName;
		private String serverAddress;

		public int getPort() {
			return port;
		}

		public RMIBean(RMIServer instance) {
			cls = instance.getClass().getName();
			port = instance.registryPort;
			bindingName = instance.bindingName;
			serverAddress = instance.serverAddress;
		}
	}

	@Override
	public Object getBean() {
		return new RMIBean(this);
	}
}
