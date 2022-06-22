package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import compute.Compute;

/**
 * Class to invoke FROM THE CLIENT
 * to make the server speak
 */
public class AskToSpeak {

  private final static String bindingName = "Compute";

  public static void main(String... args) {
    if (args.length < 3) {
      System.out.println("Arguments: [RMI Server Name] [Port] [Text to Speak]");
      System.out.printf("You have provided %d argument(s)\n", args.length);
      // Java 7 style
//      for (String s : args) {
//        System.out.println("- " + s);
//      }
      // Java 8 style
      Arrays.stream(args).forEach(arg -> System.out.println("- " + arg));
      System.exit(1);
    }
    StringBuffer sb = new StringBuffer();
    Arrays.stream(args).skip(2).forEach(arg -> sb.append(arg + " "));
    String tts = sb.toString().trim();

    System.out.println("Looking up [" + bindingName + " on " + args[0] + ":" + args[1] + "]");
    try {
      Registry registry = LocateRegistry.getRegistry(args[0], Integer.parseInt(args[1])); // Server name, port
      Compute comp = (Compute) registry.lookup(bindingName);   // RMI Name

      Speak task = new Speak(tts);
      boolean spoken = comp.executeTask(task);
      System.out.println("Spoken:" + spoken);

    } catch (Exception e) {
      System.err.println("Speak exception:");
      e.printStackTrace();
    }
  }
}
