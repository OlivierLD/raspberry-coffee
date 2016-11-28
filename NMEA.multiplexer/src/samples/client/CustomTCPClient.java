package samples.client;

import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import samples.reader.CustomSerialReader;
import samples.reader.CustomTCPReader;

/**
 * REad NMEA Data from a TCP server
 */
public class CustomTCPClient extends NMEAClient
{
  public CustomTCPClient(String s, String[] sa)
  {
    super(s, sa);
  }
  
  public void dataDetectedEvent(NMEAEvent e)
  {
    System.out.println("Received from TCP :" + e.getContent());
  }

  private static CustomTCPClient customClient = null;
  
  public static void main(String[] args)
  {
    System.out.println("CustomTCPClient invoked with " + args.length + " Parameter(s).");
    for (String s : args)
      System.out.println("CustomTCPClient prm:" + s);
    
    String prefix = null; // "GP";
    String[] array = null; // {"GVS", "GLL", "RME", "GSA", "RMC"};
    customClient = new CustomTCPClient(prefix, array);
      
    Runtime.getRuntime().addShutdownHook(new Thread() 
      {
        public void run() 
        {
          System.out.println ("Shutting down nicely.");
          customClient.stopDataRead();
        }
      });    
    customClient.setEOS("\n"); // TASK Sure?
    customClient.initClient();
    customClient.setReader(new CustomTCPReader(customClient.getListeners(), "localhost", 7001));
    customClient.startWorking();
  }

  private void stopDataRead()
  {
    if (customClient != null)
    {
      for (NMEAListener l : customClient.getListeners())
        l.stopReading(new NMEAEvent(this));
    }
  }
}