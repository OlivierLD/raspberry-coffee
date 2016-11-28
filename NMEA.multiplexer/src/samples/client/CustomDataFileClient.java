package samples.client;

import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import samples.reader.CustomFileReader;
import samples.reader.CustomSerialReader;

public class CustomDataFileClient extends NMEAClient
{
  public CustomDataFileClient(String s, String[] sa)
  {
    super(s, sa);
  }
  
  public void dataDetectedEvent(NMEAEvent e)
  {
    System.out.println("Received:" + e.getContent());
  }

  private static CustomDataFileClient customClient = null;
  
  public static void main(String[] args)
  {
    System.out.println("CustomSerialClient invoked with " + args.length + " Parameter(s).");
    for (String s : args)
      System.out.println("CustomSerialClient prm:" + s);

    String dataFile = "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";
    if (args.length > 0)
      dataFile = args[0];
    
    String prefix = null; // "*";
    String[] array = null; // {"GVS", "GLL", "RME", "GSA", "RMC"};
    customClient = new CustomDataFileClient(prefix, array);
      
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
    customClient.setReader(new CustomFileReader(customClient.getListeners(), dataFile));
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