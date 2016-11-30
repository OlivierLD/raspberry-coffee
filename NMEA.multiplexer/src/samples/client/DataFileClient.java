package samples.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import samples.reader.FileReader;

/**
 * Read a file containing logged data
 */
public class DataFileClient extends NMEAClient
{
  public DataFileClient()
  {
    super(null, null, null);
  }
  public DataFileClient(Multiplexer mux)
  {
    super(mux);
  }
  public DataFileClient(String s, String[] sa)
  {
    super(s, sa, null);
  }

  @Override
  public void dataDetectedEvent(NMEAEvent e)
  {
    if ("true".equals(System.getProperty("file.data.verbose", "false")))
      System.out.println("Received from File:" + e.getContent());
    if (multiplexer != null)
    {
      multiplexer.onData(e.getContent());
    }
  }

  private static DataFileClient nmeaClient = null;
  
  public static void main(String[] args)
  {
    System.out.println("CustomDataFileClient invoked with " + args.length + " Parameter(s).");
    for (String s : args)
      System.out.println("CustomDataFileClient prm:" + s);

    String dataFile = "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";
    if (args.length > 0)
      dataFile = args[0];
    
    nmeaClient = new DataFileClient();
      
    Runtime.getRuntime().addShutdownHook(new Thread() 
      {
        public void run() 
        {
          System.out.println ("Shutting down nicely.");
          nmeaClient.stopDataRead();
        }
      });

    nmeaClient.setEOS("\n"); // TASK Sure?
    nmeaClient.initClient();
    nmeaClient.setReader(new FileReader(nmeaClient.getListeners(), dataFile));
    nmeaClient.startWorking();
  }
}