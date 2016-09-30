package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.math.BigDecimal;
import compute.Compute;

public class ComputePi {
	public static void main(String args[]) {
		if (args.length != 3) {
			System.out.println("Arguments: [RMI Server Name] [RMI Name] [PI precision]");
			System.exit(1);
		}
//		if (System.getSecurityManager() == null) {
//			System.setSecurityManager(new SecurityManager());
//		}
		System.out.println("Looking up [" + args[1] + "]");
		try {
			Registry registry = LocateRegistry.getRegistry(args[0]); // Machine name
			Compute comp = (Compute) registry.lookup(args[1]);       // RMI Name

			Pi task = new Pi(Integer.parseInt(args[2]));             // Precision
			BigDecimal pi = comp.executeTask(task);
			System.out.println(pi);

		} catch (Exception e) {
			System.err.println("ComputePi exception:");
			e.printStackTrace();
		}
	}
}
