package raspisamples;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import raspisamples.pwm.PWMPin;

public class RealPWMLed
{
  private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

  public static String userInput(String prompt)
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
    throws InterruptedException
  {
    final GpioController gpio = GpioFactory.getInstance();

    PWMPin pin = new PWMPin(RaspiPin.GPIO_01, "OneLED", PinState.LOW);
    pin.low(); // Useless

    System.out.println("PWM, glowing up and down");
    // PWM
    pin.emitPWM(0);
    Thread.sleep(1000);
    for (int vol=0; vol<100; vol++)
    {
      pin.adjustPWMVolume(vol);
      Thread.sleep(10);
    }
    for (int vol=100; vol>=0; vol--)
    {
      pin.adjustPWMVolume(vol);
      Thread.sleep(10);
    }

    System.out.println("Enter \"S\" or \"quit\" to stop, or a volume [0..100]");
    boolean go = true;
    while (go)
    {
      String userInput = userInput("Volume > ");
      if ("S".equalsIgnoreCase(userInput) ||
          "quit".equalsIgnoreCase(userInput))
        go = false;
      else
      {
        try
        {
          int vol = Integer.parseInt(userInput);
          pin.adjustPWMVolume(vol);
        }
        catch (NumberFormatException nfe)
        {
          System.out.println(nfe.toString());
        }
      }
    }
    pin.stopPWM();
    
    Thread.sleep(1000);
    // Last blink
    System.out.println("Bye-bye");
    pin.low();
    Thread.sleep(500);
    pin.high();
    Thread.sleep(500);
    pin.low();
    
    gpio.shutdown();
  }
  
  private static void waitFor(long ms)
  {
    try
    {
      Thread.sleep(ms);
    }
    catch (InterruptedException ie)
    {
      ie.printStackTrace();
    }
  }
}
