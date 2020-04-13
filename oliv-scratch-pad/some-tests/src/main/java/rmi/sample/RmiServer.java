package rmi.sample;

import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;

public class RmiServer extends java.rmi.server.UnicastRemoteObject
		implements ServerInterface {
	private String bindingName = "rmiServer";
	private int port = 1099;
	private String serverAddress;
	private Registry registry;

	@Override
	public String execute(String str) throws RemoteException {
		System.out.println("Received: " + str);
		return ("[" + serverAddress + "]: " + str);
	}

	public RmiServer() throws RemoteException {
		try {
			serverAddress = (InetAddress.getLocalHost()).toString();
		} catch (Exception e) {
			throw new RemoteException("Can't get inet address.");
		}
		System.out.println(String.format("Server address : %s, port %d", serverAddress, port));
		try {
			registry = LocateRegistry.createRegistry(port);
			registry.rebind(bindingName, this);
		} catch (RemoteException e) {
			throw e;
		}
	}

	public static void main(String... args) {
		try {
			RmiServer server = new RmiServer();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
