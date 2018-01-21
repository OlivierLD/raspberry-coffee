package client;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.math.BigDecimal;
import compute.Compute;

/**
 * Class to invoke FROM THE CLIENT
 * to compute Pi
 */
public class ComputePi {

	private static String bindingName = "Compute";

	public static void main(String... args) {
		if (args.length != 3) {
			System.out.println("Arguments: [RMI Server Name] [Port] [PI precision]");
			System.exit(1);
		}

		System.out.println("Looking up [" + bindingName + " on " + args[0] + ":" + args[1] + "]");
		try {
			Registry registry = LocateRegistry.getRegistry(args[0], new Integer(args[1])); // Server name, port
			Remote remote = registry.lookup(bindingName);   // RMI Name
			System.out.println(String.format("Remote is a %s", remote.getClass().getName()));
			Compute comp = (Compute) remote;

			Pi task = new Pi(Integer.parseInt(args[2]));             // Precision
			BigDecimal pi = comp.executeTask(task);                  // Invoke the task on the server.
			System.out.println(pi);
		} catch (Exception e) {
			System.err.println("ComputePi exception:");
			e.printStackTrace();
		}
	}
}
