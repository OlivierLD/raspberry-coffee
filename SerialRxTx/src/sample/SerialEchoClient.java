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
  @Override
  public void connected(boolean b)
  {
    System.out.println("Serial port connected: " + b);
  }

  private int lenToRead = 0;
  private int bufferIdx = 0;
  private byte[] serialBuffer = new byte[256];

  @Override
  public void onSerialData(byte b)
  {
    System.out.println("\t\tReceived character [0x" + Integer.toHexString(b) + "]");
    serialBuffer[bufferIdx++] = (byte)(b & 0xFF);
    if (b == 0xA) // \n 
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

  public void serialOutput(byte[] mess)
  {
    if (true) // verbose...
    {
      try
      {
        String[] sa = DumpUtil.dualDump(mess);
        if (sa != null)
        {
          System.out.println("\t>>> [From Serial port] Received:");
          for (String s: sa)
            System.out.println("\t\t"+ s);                
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

  private static String userInput(String prompt)
  {
    String retString = "";
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

  public static void main(String[] args)
  {
    final SerialEchoClient mwc = new SerialEchoClient();
    final SerialCommunicator sc = new SerialCommunicator(mwc);
    sc.setVerbose(false);
    
    Map<String, CommPortIdentifier> pm = sc.getPortList();
    Set<String> ports = pm.keySet();
    if (ports.size() == 0) {
      System.out.println("No serial port found.");
      System.out.println("Did you run as administrator (sudo) ?");
    }
    System.out.println("== Serial Port List ==");
    for (String port : ports)
      System.out.println("-> " + port);
    System.out.println("======================");

    String serialPortName = System.getProperty("serial.port", "/dev/ttyUSB0");
    String baudRateStr = System.getProperty("baud.rate", "9600");
    System.out.println(String.format("Opening port %s:%s", serialPortName, baudRateStr));
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
      sc.initListener();
      
      Thread.sleep(500L);
      // Wake up!
//      for (int i=0; i<5; i++)
//      {
//        sc.writeData("\n");
//      }
//      Thread.sleep(1000L);
      System.out.println("Writing to the serial port.");

      boolean keepWorking = true;
      while (keepWorking)
      {
        String userInput = userInput("$> ");
        System.out.println(String.format("Input [%s]", userInput));
        if (userInput.equals("exit"))
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
    System.out.println("Disconnecting");
    try {  sc.disconnect(); } catch (IOException ioe) { ioe.printStackTrace(); }
    System.out.println("Done.");
  }
}
