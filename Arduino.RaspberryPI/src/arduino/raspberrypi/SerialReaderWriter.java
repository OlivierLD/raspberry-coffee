package arduino.raspberrypi;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Write data, from the Raspberry to the Arduino, through the serial port.
 * Receives a response from the Arduino.
 */ 
public class SerialReaderWriter
{
  // NMEA Style
  private static int calculateCheckSum(String str)
  {
    int cs = 0;
    char[] ca = str.toCharArray();
    for (int i=0; i<ca.length; i++)
    {
      cs ^= ca[i]; // XOR
//    System.out.println("\tCS[" + i + "] (" + ca[i] + "):" + Integer.toHexString(cs));
    }
    return cs;
  }
  
  // NMEA Style
  private static boolean validCheckSum(String data, boolean verb)
  {
    String sentence = data.trim();
    boolean b = false;    
    try
    {
      int starIndex = sentence.indexOf("*");
      if (starIndex < 0)
        return false;
      String csKey = sentence.substring(starIndex + 1);
      int csk = Integer.parseInt(csKey, 16);
      String str2validate = sentence.substring(1, sentence.indexOf("*"));
      int calcCheckSum = calculateCheckSum(str2validate);
      b = (calcCheckSum == csk);
    }
    catch (Exception ex)
    {
      if (verb) System.err.println("Oops:" + ex.getMessage());
    }
    return b;
  }

  private static boolean verbose = true;
  private static boolean getVerbose()
  {
    return verbose;
  }
  
  public static void main(String args[])
    throws InterruptedException, NumberFormatException
  {
    String port = System.getProperty("serial.port", Serial.DEFAULT_COM_PORT);
    int br = Integer.parseInt(System.getProperty("baud.rate", "9600"));
    if (args.length > 0)
    {
      try
      {
        br = Integer.parseInt(args[0]);
      }
      catch (Exception ex)
      {
        System.err.println(ex.getMessage());
      }
    }
    
    System.out.println("Serial Communication.");
    System.out.println(" ... connect using settings: " + Integer.toString(br) +  ", N, 8, 1.");
    System.out.println(" ... data received on serial port should be displayed below.");

    // create an instance of the serial communications class
    final Serial serial = SerialFactory.createInstance();

    // create and register the serial data listener
    serial.addListener(new SerialDataListener() // Listens to the Arduino
    {
      @Override
      public void dataReceived(SerialDataEvent event)
      {
        // print out the data received to the console
        String payload = event.getData();
        if (getVerbose())
        {
          if (validCheckSum(payload, false))
            System.out.print("Arduino said:" + payload);
          else
            System.out.println("\tOops! Invalid String [" + payload + "]");
        }
      }
    });

    try
    {
      System.out.println("Hit 'Q' to quit.");
      System.out.println("Hit 'V' to toggle verbose on/off.");
      System.out.println("Hit [return] when ready to start.");
      userInput("");

      System.out.println("Opening port [" + port + ":" + Integer.toString(br) + "]");
      serial.open(port, br);
      System.out.println("Port is opened.");

      final Thread me = Thread.currentThread();
      Thread userInputThread = new Thread() // Write to the Arduino on user's request (CLI)
        {
          public void run()
          {
            boolean loop = true;
            while (loop)
            {
              String userInput = "";
              userInput = userInput("So? > ");
              if ("Q".equalsIgnoreCase(userInput))
                loop = false;
              else if ("V".equalsIgnoreCase(userInput))
                verbose = !verbose;
              else
              { 
                if (serial.isOpen())
                {
                  System.out.println("\tWriting [" + userInput + "] to the serial port...");
                  try
                  {
                    serial.write(userInput);
                  }
                  catch (IllegalStateException ex)
                  {
                    ex.printStackTrace();
                  }
                }
                else
                {
                  System.out.println("Not open yet...");
                }
              }
            }
            synchronized (me)
            {
              me.notify();
            }
          }
        };
      userInputThread.start();
      
      synchronized (me)
      {
        me.wait();
      }
      System.out.println("Bye!");
      serial.close();
    }
    catch (SerialPortException ex)
    {
      System.out.println(" ==>> SERIAL SETUP FAILED : " + ex.getMessage());
      return;
    }
    System.exit(0);
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

}
