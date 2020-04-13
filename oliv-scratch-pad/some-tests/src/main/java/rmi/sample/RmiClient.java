package rmi.sample;

import java.rmi.*;
import java.rmi.registry.*;

public class RmiClient {

	private static String serverBindingName = "rmiServer";

	public static void main(String... args) {

		if (args.length != 3) {
			System.err.println("Usage is:");
			System.err.println("RmiClient serverName port message");
			throw new IllegalArgumentException();
		}

		ServerInterface rmiServer;
		Registry registry;
		String serverAddress = args[0];
		String serverPort = args[1];
		String text = args[2];
		System.out.println("Sending " + text + " to " + serverAddress + ":" + serverPort);
		try {
			// get the registry
			registry = LocateRegistry.getRegistry(serverAddress, (new Integer(serverPort)).intValue());
			// look up the remote object
			rmiServer = (ServerInterface) (registry.lookup(serverBindingName));
			// call the remote method
			String response = rmiServer.execute(text);
			System.out.println("Response:" + response);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
}
