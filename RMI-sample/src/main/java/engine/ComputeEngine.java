package engine;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import compute.Compute;
import compute.Task;

public class ComputeEngine extends UnicastRemoteObject implements Compute {

	private Registry registry = null;
	private final int registryPort = 1_099;
	private String serverAddress = "";
	private final String bindingName = "Compute";


	public ComputeEngine() throws RemoteException {
		try {
			serverAddress = (InetAddress.getLocalHost()).toString();
		} catch(Exception e) {
			throw new RemoteException("Can't get inet address.");
		}
		System.out.printf("Server address : %s, port %d\n", serverAddress, registryPort);
		try{
			registry = LocateRegistry.createRegistry(registryPort);
			registry.rebind(bindingName, this);
		} catch (RemoteException e) {
			throw e;
		}
	}

	@Override
	public <T> T executeTask(Task<T> t) throws RemoteException {
		return t.execute();
	}

	public static void main(String... args) throws RemoteException {
		ComputeEngine ce = new ComputeEngine();
	}
}
