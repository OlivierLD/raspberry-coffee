package samples.client;

import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import samples.reader.CustomSerialReader;

public class CustomSerialClient extends NMEAClient
{
  public CustomSerialClient(String s, String[] sa)
  {
    super(s, sa);
  }
  
  public void dataDetectedEvent(NMEAEvent e)
  {
    System.out.println("Received:" + e.getContent());
  }

  private static CustomSerialClient customClient = null;
  
  public static void main(String[] args)
  {
    System.out.println("CustomSerialClient invoked with " + args.length + " Parameter(s).");
    for (String s : args)
      System.out.println("CustomSerialClient prm:" + s);
    
    String commPort = "/dev/ttyUSB0"; // "COM1";
    if (args.length > 0)
      commPort = args[0];
    
//  String prefix = "II";
//  String[] array = {"HDM", "GLL", "XTE", "MWV", "VHW"};
    String prefix = "GP";
    String[] array = {"GVS", "GLL", "RME", "GSA", "RMC"};
    customClient = new CustomSerialClient(prefix, array);
      
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
    customClient.setReader(new CustomSerialReader(customClient.getListeners(), commPort));
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