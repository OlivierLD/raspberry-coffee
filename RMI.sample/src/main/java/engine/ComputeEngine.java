package engine;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import compute.Compute;
import compute.Task;

public class ComputeEngine extends UnicastRemoteObject implements Compute {

	private Registry registry     = null;
	private int registryPort      = 1099;
	private String serverAddress = "";
	private String bindingName = "Compute";


	public ComputeEngine() throws RemoteException {
		try {
			serverAddress = (InetAddress.getLocalHost()).toString();
		} catch(Exception e) {
			throw new RemoteException("Can't get inet address.");
		}
		System.out.println(String.format("Server address : %s, port %d", serverAddress, registryPort));
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
