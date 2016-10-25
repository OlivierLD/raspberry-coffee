package sample;

import gnu.io.CommPortIdentifier;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.util.Map;

import java.util.Set;

import sample.util.DumpUtil;

import serial.io.SerialCommunicator;
import serial.io.SerialIOCallbacks;

public class SerialEchoClient implements SerialIOCallbacks
{
  private static boolean verbose = "true".equals(System.getProperty("verbose", "false"));
  @Override
  public void connected(boolean b)
  {
    System.out.println("Serial port connected: " + b);
  }

  private int lenToRead = 0;
  private int bufferIdx = 0;
  private byte[] serialBuffer = new byte[4096]; // byte[256];

  @Override
  public void onSerialData(byte b)
  {
    System.out.println("\t\tReceived character [0x" + Integer.toHexString(b & 0xFF) + "]");
    serialBuffer[bufferIdx++] = (byte)(b & 0xFF);
    if (b == 0xA) // \n , EOM
    {
      // Message completed
      byte[] mess = new byte[bufferIdx];
      for (int i=0; i<bufferIdx; i++)
        mess[i] = serialBuffer[i];
      serialOutput(mess);
      // Reset
      lenToRead = 0;
      bufferIdx = 0;
    }
  }

  @Override
  public void onSerialData(byte[] ba, int len)
  {
    System.arraycopy(ba, 0, serialBuffer, bufferIdx, len);
    bufferIdx += len;

    String[] messages = new String(serialBuffer, 0, bufferIdx).split("\n"); // Full lines end with \r\n
    for (String mess : messages)
    {
      serialOutput(mess);
    }
    bufferIdx = 0;
  }

  public void serialOutput(String mess)
  {
    serialOutput(mess.getBytes());
  }

  public void serialOutput(byte[] mess)
  {
//  System.out.println(String.format(">>> Got %d bytes", mess.length));
    if (verbose)
    {
      try
      {
        String[] sa = DumpUtil.dualDump(mess);
        if (sa != null)
        {
//        System.out.println("\t>>> [From Serial port] Received:");
          for (String s: sa)
            System.out.println("\t\t"+ s);                
        }
        else
        {
          System.out.println(String.format("sa is null (mess %d byte(s)]...", mess.length));
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    else // Standard mode, no hex dump.
    {
      int offset = 0;
      while (mess[offset] == 0xA || mess[offset] == 0xD)
        offset++;
      String str = new String(mess, offset, mess.length - offset);
      System.out.print(str.replace('\r', '\n'));
    }
  }

  private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

  private static String userInput(String prompt)
  {
    String retString = "";
    if (prompt != null)
      System.err.print(prompt);
    try
    {
      retString = stdin.readLine();
    }
    catch(Exception e)
    {
      System.out.println(e);
      String s;
      try
      {
        s = userInput("<Oooch/>");
      }
      catch(Exception exception)
      {
        exception.printStackTrace();
      }
    }
    return retString;
  }

  /**
   * Can work with the RPi Serial Console (USB).
   *
   * Pin #2  5V0 Red                             #1 . . #2
   * Pin #6  Gnd Black                           #3 . . #4
   * Pin #8  Tx  White                           #5 . . #6
   * Pin #10 Rx  Green                           #7 . . #8
   *                                             #9 . . #10
   * @param args                              etc   . .
   */
  public static void main(String[] args)
  {
    final SerialEchoClient mwc = new SerialEchoClient();
    final SerialCommunicator sc = new SerialCommunicator(mwc);
    sc.setVerbose(verbose);
    
    Map<String, CommPortIdentifier> pm = sc.getPortList();
    Set<String> ports = pm.keySet();
    if (ports.size() == 0) {
      System.out.println("No serial port was found.");
      System.out.println("Did you run as administrator (sudo) ?");
    }
    System.out.println("== Serial Port List ==");
    for (String port : ports)
      System.out.println("-> " + port);
    System.out.println("======================");

    String serialPortName = System.getProperty("serial.port", "/dev/ttyUSB0");
    String baudRateStr = System.getProperty("baud.rate", "9600");
    System.out.println(String.format("Opening port %s:%s ...", serialPortName, baudRateStr));
    CommPortIdentifier serialPort = pm.get(serialPortName);
    if (serialPort == null)
    {
      System.out.println(String.format("Port %s not found, aborting", serialPortName));
      System.exit(1);
    }
    try 
    {
      sc.connect(serialPort, "SerialRxTx", Integer.parseInt(baudRateStr));
      boolean b = sc.initIOStream();
      System.out.println("IO Streams " + (b?"":"NOT ") + "initialized");
      if (verbose) {
        System.out.println("Verbose: ON");
      }
      sc.initListener();
      
      Thread.sleep(500L);

      System.out.println("Writing to the serial port.");

      boolean keepWorking = true;
      while (keepWorking)
      {
        String userInput = userInput(null);
        System.out.println(String.format("Input [%s]", userInput));
        if (userInput.equals("quit"))
        {
          System.out.println("Bye!");
          keepWorking = false;
        }
        else
        {
          sc.writeData(userInput + "\n");
//        System.out.println("Data written to the serial port.");
        }
      }
      System.out.println("Exiting program.");
    }
    catch (Exception ex) 
    {
      ex.printStackTrace();
    }
    System.out.println("Disconnecting...");
    if (false && sc.isConnected()) {
      try {
        sc.disconnect();
      } catch (IOException ioe) {
        ioe.printStackTrace();
      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        System.out.println("Ooops!");
      }
    }
    System.out.println("Done.");
  }
}
