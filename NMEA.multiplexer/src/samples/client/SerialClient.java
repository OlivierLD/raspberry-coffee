package samples.client;

import nmea.api.NMEAClient;
import nmea.api.NMEAEvent;
import samples.reader.SerialReader;

/**
 * Read NMEA Data from a Serial port
 */
public class SerialClient extends NMEAClient
{
  public SerialClient()
  {
    this(null, null);
  }
  public SerialClient(String s, String[] sa)
  {
    super(s, sa);
  }

  @Override
  public void dataDetectedEvent(NMEAEvent e)
  {
    if ("true".equals(System.getProperty("serial.data.verbose", "false")))
      System.out.println("Received from Serial:" + e.getContent());
    if (parent != null)
    {
      parent.onData(e.getContent());
    }
  }

  private static SerialClient nmeaClient = null;
  
  public static void main(String[] args)
  {
    System.out.println("CustomSerialClient invoked with " + args.length + " Parameter(s).");
    for (String s : args)
      System.out.println("CustomSerialClient prm:" + s);
    
//  String commPort = "/dev/ttyUSB0"; // "COM1";
    String commPort = "/dev/tty.usbserial"; // Mac
    if (args.length > 0)
      commPort = args[0];
    
    nmeaClient = new SerialClient();
      
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
    nmeaClient.setReader(new SerialReader(nmeaClient.getListeners(), commPort, 4800));
    nmeaClient.startWorking();
  }
}