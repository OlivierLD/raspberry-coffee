package nmeaproviders.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import nmeaproviders.reader.RandomReader;

/**
 * Read a file containing logged data
 */
public class RandomClient extends NMEAClient
{
  public RandomClient()
  {
    super(null, null, null);
  }
  public RandomClient(Multiplexer mux)
  {
    super(mux);
  }
  public RandomClient(String s, String[] sa)
  {
    super(s, sa, null);
  }

  @Override
  public void dataDetectedEvent(NMEAEvent e)
  {
    if ("true".equals(System.getProperty("rnd.data.verbose", "false")))
      System.out.println("Received from RND:" + e.getContent());
    if (multiplexer != null)
    {
      multiplexer.onData(e.getContent());
    }
  }

  private static RandomClient nmeaClient = null;
  
  public static void main(String[] args)
  {
    System.out.println("RandomClient invoked with " + args.length + " Parameter(s).");
    for (String s : args)
      System.out.println("RandomClient prm:" + s);

    nmeaClient = new RandomClient();
      
    Runtime.getRuntime().addShutdownHook(new Thread() 
      {
        public void run() 
        {
          System.out.println ("Shutting down nicely.");
          nmeaClient.stopDataRead();
        }
      });

    nmeaClient.initClient();
    nmeaClient.setReader(new RandomReader(nmeaClient.getListeners()));
    nmeaClient.startWorking();
  }
}