package gpio01;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class GPIO02led {
    public static void main(String... args)
            throws InterruptedException {

        System.out.println("GPIO Control - pin 00 & 02 ... started.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #00 & #02 as an output pin and turn on
        final GpioPinDigitalOutput pin00 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "RedLed", PinState.HIGH);
        final GpioPinDigitalOutput pin02 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "GreenLed", PinState.HIGH);

        Thread.sleep(1_000);
        System.out.println("Blinking red fast...");
        for (int i = 0; i < 100; i++) {
            pin00.toggle();
            Thread.sleep(50);
        }
        System.out.println("Blinking green fast...");
        for (int i = 0; i < 100; i++) {
            pin02.toggle();
            Thread.sleep(50);
        }

        pin00.low();
        pin02.low();
        Thread.sleep(1_000);
        pin00.high();
        System.out.println("Blinking red & green fast...");
        for (int i = 0; i < 100; i++) {
            pin00.toggle();
            pin02.toggle();
            Thread.sleep(50);
        }

        pin00.high();
        pin02.low();
        Thread.sleep(100);
        pin02.high();
        Thread.sleep(1_000);

        pin00.low();
        pin02.low();

        Thread.sleep(100);

        pin00.pulse(500, true); // set second argument to 'true' use a blocking call
        pin02.pulse(500, true); // set second argument to 'true' use a blocking call

        Thread.sleep(100);

        pin00.pulse(500, false); // set second argument to 'true' use a blocking call
        Thread.sleep(100);
        pin02.pulse(500, false); // set second argument to 'true' use a blocking call
        Thread.sleep(1_000);

        // All on
        pin00.high();
        pin02.high();
        Thread.sleep(1_000);

        pin00.low();
        pin02.low();
        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();
    }
}
