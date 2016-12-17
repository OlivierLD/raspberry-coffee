package nmea.suppliers.rmi.client;

import nmea.suppliers.rmi.RMIServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * An example of the way to invoke the RMI server
 */
public class SampleRMIClient {

	private static String bindingName = "RMI-NMEA";

	public static void main(String args[]) {
		String name;
		String port;

		if (args.length != 2) {
			System.out.println("Arguments: [RMI Server Name] [Port]");
			name = "olediouris-mbp"; // "10.10.226.181";
			port = "1099";
		} else {
			name = args[0];
			port = args[1];
		}

		System.out.println("Looking up [" + bindingName + " on " + name + ":" + port + "]");
		try {
			Registry registry = LocateRegistry.getRegistry(name, new Integer(port)); // Server name, port
			RMIServer comp = (RMIServer) registry.lookup(bindingName);   // RMI Name

			LastString task = new LastString();
			String last = comp.executeTask(task);
			System.out.println(last);
		} catch (Exception e) {
			System.err.println("Compute Last String exception:");
			e.printStackTrace();
		}
	}
}
