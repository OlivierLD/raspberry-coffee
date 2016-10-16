package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.math.BigDecimal;
import compute.Compute;

/**
 * Class to invoke FROM THE CLIENT
 * to make the server speak
 */
public class AskToSpeak {

  private static String bindingName = "Compute";

  public static void main(String args[]) {
    if (args.length != 3) {
      System.out.println("Arguments: [RMI Server Name] [Port] [Text to Speak]");
      System.exit(1);
    }

    System.out.println("Looking up [" + bindingName + " on " + args[0] + ":" + args[1] + "]");
    try {
      Registry registry = LocateRegistry.getRegistry(args[0], new Integer(args[1])); // Server name, port
      Compute comp = (Compute) registry.lookup(bindingName);   // RMI Name

      Speak task = new Speak(args[2]);
      boolean spoken = comp.executeTask(task);
      System.out.println("Spoken:" + spoken);

    } catch (Exception e) {
      System.err.println("Speak exception:");
      e.printStackTrace();
    }
  }
}
