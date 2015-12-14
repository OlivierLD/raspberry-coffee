package rangesensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import java.text.DecimalFormat;
import java.text.Format;

/**
 * @see https://www.modmypi.com/blog/hc-sr04-ultrasonic-range-sensor-on-the-raspberry-pi
 * 
 */
public class HC_SR04
{
  private final static Format DF22 = new DecimalFormat("#0.00");
  private final static Format DF_N = new DecimalFormat("#.##########################");

  private final static double SOUND_SPEED = 34029;  // 34300;         // in cm, 340.29 m/s
  private final static double DIST_FACT   = SOUND_SPEED / 2; // round trip
  private final static int MIN_DIST = 3; // en cm
  
  private static boolean verbose = false;
  private final static long BILLION      = (long)10E9;
  private final static int TEN_MICRO_SEC = 10 * 1000; // In Nano secs
  
  public static void main(String[] args)
    throws InterruptedException
  {
    System.out.println("GPIO Control - Range Sensor HC-SR04.");
    System.out.println("Will stop is distance is smaller than " + MIN_DIST + " cm");

    verbose = "true".equals(System.getProperty("verbose", "false"));
    
    // create gpio controller
    final GpioController gpio = GpioFactory.getInstance();

    final GpioPinDigitalOutput trigPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Trig", PinState.LOW);
    final GpioPinDigitalInput  echoPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05,  "Echo");
    
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        System.out.println("Oops!");
        gpio.shutdown();
        System.out.println("Exiting nicely.");
      }       
    });
    
    System.out.println(">>> Waiting for the sensor to be ready (2s)...");
    Thread.sleep(2000);

    boolean go = true;
    System.out.println("Looping until the distance is less than " + MIN_DIST + " cm");
    while (go)
    {
      trigPin.low();
      try { Thread.sleep(500); } catch (Exception ex) { ex.printStackTrace(); } 
      
      // Just to check...
      if (echoPin.isHigh())
        System.out.println(">>> !! Before sending signal, echo PIN is " + (echoPin.isHigh() ? "High" : "Low"));
          
      trigPin.high();
      // 10 microsec to trigger the module  (8 ultrasound bursts at 40 kHz) 
      // https://www.dropbox.com/s/615w1321sg9epjj/hc-sr04-ultrasound-timing-diagram.png
      try { Thread.sleep(0, TEN_MICRO_SEC); } catch (Exception ex) { ex.printStackTrace(); } 
      trigPin.low();

      // Wait for the signal to return
      while (echoPin.isLow()); // && (start == 0 || (start != 0 && (start - top) < BILLION)))
      long start = System.nanoTime();
      // There it is, the echo comes back.
      while (echoPin.isHigh());
      long end   = System.nanoTime();

  //  System.out.println(">>> TOP: start=" + start + ", end=" + end);
  //  System.out.println("Nb Low Check:" + nbLowCheck + ", Nb High Check:" + nbHighCheck);
      
      if (end > start) //  && start > 0)
      {
        double pulseDuration = (double)(end - start) / (double)BILLION; // in seconds
//      System.out.println("Duration:" + (end - start) + " nanoS"); // DF_N.format(pulseDuration));
        double distance = pulseDuration * DIST_FACT;
        if (distance < 1000) // Less than 10 meters
          System.out.println("Distance: " + DF22.format(distance) + " cm. (" + distance + "), Duration:" + (end - start) + " nanoS"); // + " (" + pulseDuration + " = " + end + " - " + start + ")");
        else
          System.out.println("   >>> Too far:" + DF22.format(distance) + " cm.");
        if (distance > 0 && distance < MIN_DIST)
          go = false;
        else
        {
          if (distance < 0 && verbose)
            System.out.println("Dist:" + distance + ", start:" + start + ", end:" + end);
          try { Thread.sleep(1000L); } catch (Exception ex) {}
        }
      }
      else
      {
        if (verbose)
          System.out.println("Hiccup! start:" + start + ", end:" + end);
        try { Thread.sleep(500L); } catch (Exception ex) {}
      }
    }
    System.out.println("Done.");
    trigPin.low(); // Off

    gpio.shutdown();
  }
}
