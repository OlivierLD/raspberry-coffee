package gpio01;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class GPIO01led {
  public static void main(String... args)
    throws InterruptedException {

    System.out.println("GPIO Control - pin 01 ... started.");

    // create gpio controller
    final GpioController gpio = GpioFactory.getInstance();

    // provision gpio pin #01 as an output pin and turn on
    final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.HIGH);
    System.out.println("--> GPIO state should be: ON");

    Thread.sleep(5_000);

    // turn off gpio pin #01
    pin.low();
    System.out.println("--> GPIO state should be: OFF");

    Thread.sleep(5_000);

    // toggle the current state of gpio pin #01 (should turn on)
    pin.toggle();
    System.out.println("--> GPIO state should be: ON");

    Thread.sleep(5_000);

    // toggle the current state of gpio pin #01  (should turn off)
    pin.toggle();
    System.out.println("--> GPIO state should be: OFF");

    Thread.sleep(5_000);

    // turn on gpio pin #01 for 1 second and then off
    System.out.println("--> GPIO state should be: ON for only 1 second");
    pin.pulse(1_000, true); // set second argument to 'true' use a blocking call

    // stop all GPIO activity/threads by shutting down the GPIO controller
    // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
    gpio.shutdown();
  }
}
