package samples.reader;

import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAReader;

import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * A Simulator, taking its inputs from a file
 */
public class CustomReader extends NMEAReader
{
  public CustomReader(ArrayList<NMEAListener> al)
  {
    super(al);
  }
  
  public void read()
  {
    // Simulation
    super.enableReading();
    String fileName = "./sample.data/2010-11-08.Nuku-Hiva-Tuamotu.nmea";
    try
    {
      FileInputStream fis = new FileInputStream(fileName);
      while (canRead())
      {
        double size = Math.random();
        int dim = (int)(750 * size);
        byte[] ba = new byte[dim];
        int l = fis.read(ba);
//      System.out.println("Read " + l);
        if (l != -1 && dim > 0)
        {
          fireDataRead(new NMEAEvent(this, new String(ba)));
          try { Thread.sleep(500); } catch (Exception ignore) {}
        }
        else
        {
          System.out.println("===== Reseting Reader =====");
          fis.close();
          fis = new FileInputStream(fileName);
        }
      }
    }
    catch (Exception e)
    {
     e.printStackTrace();
    }
  }
}