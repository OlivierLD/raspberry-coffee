package weatherstation.samples;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import java.text.DecimalFormat;
import java.text.Format;

import weatherstation.SDLWeather80422;

import weatherstation.SDLWeather80422.AdcMode;
import weatherstation.SDLWeather80422.SdlMode;

public class LoopTest
{
  private static boolean go = true;
  public static void main(String[] args)
  {
    final Thread coreThread = Thread.currentThread();
    
    Runtime.getRuntime().addShutdownHook(new Thread()
     {
       public void run()
       {
         System.out.println("\nUser interrupted.");
         go = false;
         synchronized (coreThread)
         {
           coreThread.notify();
         }
         System.out.println("Unleashed");
       }
     });
      
    while (go)
    {
      System.out.println("Blah");
      try 
      { 
        synchronized (coreThread)
        {
          coreThread.wait(5000L); 
        }
      } 
      catch (Exception ex) { ex.printStackTrace(); }
    }
    System.out.println("Done.\n\n");
  }
}
