package mindwave.samples.pi;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;

import java.io.BufferedWriter;

import java.io.FileWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mindwave.MindWaveController;
import mindwave.MindWaveCallbacks;

import mindwave.SerialCommunicatorInterface;

import utils.StringUtils;

public class ClientOne implements MindWaveCallbacks,
                                  SerialCommunicatorInterface
{
  private static List<Short> rawWaves = null;

  @Override
  public void mindWaveConnected(MindWaveController.DeviceID did)
  {
    System.out.println("Connected to Device ID: 0x" + StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0"));
  }

  @Override
  public void mindWaveDisconnected(MindWaveController.DeviceID did)
  {
    System.out.println("Disconnected from Device ID: 0x" + StringUtils.lpad(Integer.toHexString(did.getID() & 0xFFFF), 4, "0"));
  }

  @Override
  public void mindWaveStandby(MindWaveController.StbyStatus ss)
  {
    System.out.println("Status:" + (ss.getStatus() == MindWaveController.STBY_STATUS_STBY ? "Stand By" : (ss.getStatus() == MindWaveController.STBY_STATUS_TRYING ? "Trying..." : "Unknown")));
  }

  @Override
  public void mindWaveAccessDenied()
  {
    System.out.println("Access denied");
  }

  @Override
  public void mindWaveNotFound()
  {
    System.out.println("Not found");
  }

  @Override
  public void mindWaveRawWave(MindWaveController.RawWave rw)
  {
    System.out.println("Raw Wave value:" + rw.getValue());
    rawWaves.add(rw.getValue());
  }

  @Override
  public void mindWavePoorSignal(MindWaveController.PoorSignal ps)
  {
    System.out.println("Poor signal:" + ps.getVal() + "/255");
  }

  @Override
  public void mindWaveBatteryLevel(MindWaveController.BatteryLevel bl)
  {
    System.out.println("Battery Level:" + bl.getVal() + "/255");
  }

  @Override
  public void mindWaveHeartRate(MindWaveController.HeartRate hr)
  {
    System.out.println("Heart Rate:" + hr.getVal() + "/255");
  }

  @Override
  public void mindWave8BitRaw(MindWaveController.EightBitRaw ebr)
  {
    System.out.println("8-bit raw signal:" + ebr.getVal() + "/255");
  }

  @Override
  public void mindWaveRawMarker(MindWaveController.RawMarker rm)
  {
    System.out.println("Raw Marker:" + rm.getVal() + "/255");
  }

  @Override
  public void mindWaveAttention(MindWaveController.Attention att)
  {
    System.out.println("Attention:" + att.getVal() + "/100");
  }

  @Override
  public void mindWaveMeditation(MindWaveController.Meditation med)
  {
    System.out.println("Meditation:" + med.getVal() + "/100");
  }

  @Override
  public void mindWaveAsicEegPower(MindWaveController.AsicEegPower aep)
  {
    System.out.print("AsicEegPower: ");
    int[] values = aep.getValues();
    for (int v : values)
      System.out.print(v + " ");
    System.out.println();
  }

  @Override
  public void mindWaveUnknowType(byte t)
  {
    System.out.println("Unknown type [" + StringUtils.lpad(Integer.toHexString(t & 0xFF), 2, "0") + "]");
  }

  @Override
  public void mindWaveError(Throwable t)
  {
    t.printStackTrace();
  }

  @Override
  public boolean isSerialOpen()
  {
    return serial.isOpen();
  }

  @Override
  public void writeSerial(byte b)
  {
    try
    {
      serial.write(b);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public void flushSerial()
  {
    try
    {
      serial.flush();
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  private void closeAll(MindWaveController mwc) throws IOException
  {
    mwc.disconnectHeadSet();
    while (mwc.isConnected())
      MindWaveController.delay(1f);
    System.out.println("Disconnected. Done");

    stopReading();
    closeSerial();
  }

  private static void dumpRawValues()
  {
    try
    {
      BufferedWriter bw = new BufferedWriter(new FileWriter("raw.csv"));
      for (int v : rawWaves)
        bw.write(Integer.toString(v) + "\n");
      bw.close();
      System.out.println("raw.csv created");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private final static Serial serial = SerialFactory.createInstance(); // PI4J Serial manager

  private static boolean verbose = "true".equals(System.getProperty("mindwave.verbose", "false"));
  public static void setVerbose(boolean b) { verbose = b; }
  public static boolean getVerbose() { return verbose; }

  private static boolean readSerial = true;
  public static boolean keepReading() { return readSerial; }
  public void stopReading() { readSerial = false; }
  public void closeSerial() throws IOException { serial.close(); }

  public static void main(String... args) throws IOException
  {
    rawWaves = new ArrayList<Short>();

    final ClientOne c1 = new ClientOne();
    final MindWaveController mwc = new MindWaveController(c1, c1);
    System.out.println("Connection...");

    serial.open("/dev/ttyUSB0", 115200);

    Thread serialReader = new Thread()
    {
      private byte[] serialBuffer = new byte[256];

      public void run()
      {
        int lenToRead = 0;
        int bufferIdx = 0;
        while (keepReading())
        {
          try
          {
            while (serial.available() > 0)
            {
              char c = (char)0; // serial.read(); // TODO Fix that
              c &= 0xFF;
              serialBuffer[bufferIdx++] = (byte)c;
              if (bufferIdx == 1 && serialBuffer[0] != MindWaveController.SYNC)
                bufferIdx = 0;
              if (bufferIdx == 2 && (serialBuffer[0] != MindWaveController.SYNC || serialBuffer[1] != MindWaveController.SYNC))
                bufferIdx = 0;
              if (bufferIdx == 3)
                lenToRead = serialBuffer[2];
              if (bufferIdx > 3 && bufferIdx == (lenToRead + 3 + 1)) // 3: Payload start, 1: ChkSum
              {
                // Message completed
                byte[] mess = new byte[bufferIdx];
                for (int i=0; i<bufferIdx; i++)
                  mess[i] = serialBuffer[i];
                mwc.mwOutput(mess);
                // Reset
                lenToRead = 0;
                bufferIdx = 0;
              }
            }
          }
          catch (IllegalStateException ise)
          {
            ise.printStackTrace();
          }
          catch (IOException ioe)
          {
            ioe.printStackTrace();
          }
          catch (Exception ex)
          {
            c1.mindWaveError(ex);
    //      System.err.println("Serial Thread:" + ex.toString());
            lenToRead = 0;
            bufferIdx = 0;
          }
        }
      }
    };
    serialReader.start();

    final Thread waiter = Thread.currentThread();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      synchronized (waiter)
      {
        // Hanging up.
        try
        {
          c1.closeAll(mwc);
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
        dumpRawValues();
        waiter.notify();
      }
      System.out.println("Released Waiter...");

    }, "Shutdown Hook"));

//  short hID = (short)0x9228;
//  mwc.connectHeadSet(hID);
    mwc.connectHeadSet();

    while (!mwc.isConnected())
      MindWaveController.delay(1f);
    System.out.println("Connected!");

    // Some time to live here
    synchronized (waiter)
    {
      try
      {
        waiter.wait();
      }
      catch (InterruptedException ie)
      {
        ie.printStackTrace();
      }
      System.out.println("Waiter released.");
    }
  }
}
