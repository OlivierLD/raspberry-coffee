package samples.client;

import nmea.api.Multiplexer;
import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import samples.reader.TCPReader;

/**
 * Read NMEA Data from a TCP server
 */
public class TCPClient extends NMEAClient
{
  public TCPClient()
  {
    super();
  }
  public TCPClient(Multiplexer mux) { super(mux); }
  public TCPClient(String s, String[] sa)
  {
    super(s, sa);
  }

  @Override
  public void dataDetectedEvent(NMEAEvent e)
  {
    if ("true".equals(System.getProperty("tcp.data.verbose", "false")))
      System.out.println("Received from TCP :" + e.getContent());
    if (multiplexer != null)
    {
      multiplexer.onData(e.getContent());
    }
  }

  private static TCPClient nmeaClient = null;
  
  public static void main(String[] args)
  {
    System.out.println("CustomTCPClient invoked with " + args.length + " Parameter(s).");
    for (String s : args)
      System.out.println("CustomTCPClient prm:" + s);

    String serverName = "192.168.1.1";
    
    nmeaClient = new TCPClient();
      
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
    nmeaClient.setReader(new TCPReader(nmeaClient.getListeners(), serverName, 7001));
    nmeaClient.startWorking();
  }
}