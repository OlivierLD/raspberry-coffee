package twoleds;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

import com.pi4j.io.gpio.RaspiPin;

import twoleds.led.OneLed;

public class MainController
{
  public static void main(String... args)
  {
    GpioController gpio = GpioFactory.getInstance();
    OneLed yellowLed = new OneLed(gpio, RaspiPin.GPIO_01, "yellow");
    OneLed greenLed  = new OneLed(gpio, RaspiPin.GPIO_04, "green");

    long step = 50L;

    for (int i=0; i<10; i++)
    {
      yellowLed.on();
      try { Thread.sleep(5 * step); } catch (InterruptedException ie) {}
      yellowLed.off();
      greenLed.on();
      try { Thread.sleep(5 * step); } catch (InterruptedException ie) {}
      yellowLed.on();
      try { Thread.sleep(10 * step); } catch (InterruptedException ie) {}
      yellowLed.off();
      greenLed.off();
      try { Thread.sleep(step); } catch (InterruptedException ie) {}
    }
    gpio.shutdown();
  }
}
