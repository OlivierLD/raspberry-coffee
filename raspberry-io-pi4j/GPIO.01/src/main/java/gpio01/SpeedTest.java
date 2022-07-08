package gpio01;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class SpeedTest {
    public static void main(String... args)
            throws InterruptedException {

        System.out.println("GPIO Control - Speed test.");

        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin01 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "One", PinState.LOW);
        final GpioPinDigitalOutput pin02 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "Two", PinState.LOW);
        final GpioPinDigitalOutput pin03 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "Three", PinState.LOW);

        Thread.sleep(1_000);
        long before = System.currentTimeMillis();
        pin01.toggle();
        long after = System.currentTimeMillis();
        System.out.println("Toggle took " + Long.toString(after - before) + " ms.");

        Thread.sleep(500);
        System.out.println("Pulse...");
        for (int i = 0; i < 100; i++) {
            pin01.pulse(75, false); // set second argument to 'true' use a blocking call
            pin02.pulse(75, false); // set second argument to 'true' use a blocking call
            pin03.pulse(75, false); // set second argument to 'true' use a blocking call
            Thread.sleep(100); // 1/10 s
        }
        System.out.println("Done");
        pin01.low(); // Off

        gpio.shutdown();
    }
}
