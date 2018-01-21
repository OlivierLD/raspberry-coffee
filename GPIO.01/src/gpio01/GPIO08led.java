package gpio01;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class GPIO08led
{
  public static void main(String... args)
    throws InterruptedException
  {

    System.out.println("GPIO Control ...started.");

    // create gpio controller
    final GpioController gpio = GpioFactory.getInstance();

    final GpioPinDigitalOutput pin00 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "00", PinState.HIGH);
    final GpioPinDigitalOutput pin01 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "01", PinState.HIGH);
    final GpioPinDigitalOutput pin02 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "02", PinState.HIGH);
    final GpioPinDigitalOutput pin03 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "03", PinState.HIGH);
    final GpioPinDigitalOutput pin04 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "04", PinState.HIGH);
    final GpioPinDigitalOutput pin05 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "05", PinState.HIGH);
    final GpioPinDigitalOutput pin06 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "06", PinState.HIGH);
    final GpioPinDigitalOutput pin07 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "07", PinState.HIGH);

    final GpioPinDigitalOutput[] ledArray = { pin00, pin01, pin02, pin03, pin04, pin05, pin06, pin07 };

    System.out.println("Down an Up");
    // Down
    Thread.sleep(1_000);
    for (int i=0; i<ledArray.length; i++)
    {
      ledArray[i].toggle();
      Thread.sleep(100);
    }
    Thread.sleep(1_000);
    // Up
    for (int i=0; i<ledArray.length; i++)
    {
      ledArray[ledArray.length - 1 - i].toggle();
      Thread.sleep(100);
    }

    System.out.println("One only");
    // Down
    Thread.sleep(1_000);
    for (int i=0; i<ledArray.length; i++)
    {
      oneOnly(ledArray, ledArray[i]);
      Thread.sleep(100);
    }
    Thread.sleep(1_000);
    // Up
    for (int i=0; i<ledArray.length; i++)
    {
      oneOnly(ledArray, ledArray[ledArray.length - 1 - i]);
      Thread.sleep(100);
    }

    System.out.println("Messy...");
    Thread.sleep(1_000);
    // Big mess
    for (int i=0; i<1_000; i++)
    {
      int idx = (int)(Math.random() * 8);
      ledArray[idx].toggle();
      Thread.sleep(50);
    }

    System.out.println("Down and Up, closing.");
    // Down
    Thread.sleep(500);
    for (int i=0; i<ledArray.length; i++)
    {
      oneOnly(ledArray, ledArray[i]);
      Thread.sleep(100);
    }
    // Up
    for (int i=0; i<ledArray.length; i++)
    {
      oneOnly(ledArray, ledArray[ledArray.length - 1 - i]);
      Thread.sleep(100);
    }

    System.out.println("Done.");
    Thread.sleep(1_000);
    // Everything off
    for (int i=0; i<ledArray.length; i++)
      ledArray[i].low();

    gpio.shutdown();
  }

  private static void oneOnly(GpioPinDigitalOutput[] allLeds, GpioPinDigitalOutput theOneOn)
  {
    for (int i=0; i<allLeds.length; i++)
    {
      if (allLeds[i].equals(theOneOn))
        allLeds[i].high();
      else
        allLeds[i].low();
    }
  }
}
