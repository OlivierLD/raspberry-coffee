package diozerotests;

import com.diozero.api.RuntimeIOException;
import com.diozero.devices.LED;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * GPIO Pin numbers, see https://www.diozero.com/api/gpio.html
 * PINs are BCM numbers.
 */

public class TestToggleLed {

    // PINs are BCM numbers.
    static int ledPin    = 18; // 8 does not work on RPi 4B (4gb RAM) (even if it is an OUT pin...), 18, 22 do.

    private final static String LED_PIN_PREFIX = "--led-pin:";
    /**
     * @param args Optional --led-pin:XX (XX: BCM numbers)
     */
    public static void main(String... args) {
        System.out.println("Starting diozero test.");

        Arrays.stream(args).forEach(arg -> {
            if (arg.startsWith(LED_PIN_PREFIX)) {
                ledPin = Integer.parseInt(arg.substring(LED_PIN_PREFIX.length()));
            } else {
                System.out.printf("Un-managed prefix %s\n", arg);
            }
        });

        System.out.printf("WIll use ledPin %d\n.", ledPin);
        System.out.println(":Ctrl-C to Stop the test");

        AtomicBoolean keepLooping = new AtomicBoolean(true);

        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            keepLooping.set(false);
            synchronized (currentThread) {
//                currentThread.notify(); // No thread is waiting...
                try {
                    currentThread.join();
                    System.out.println("... Gone");
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }, "Oops"));

        try (LED led = new LED(ledPin)) { // With Resource ;)
            while (keepLooping.get()) {
                System.out.printf("Toggling Led %d\n", ledPin);
                led.toggle();
                SleepUtil.sleepSeconds(1);
                // utils.TimeUtil.delay(1f);
            }
        } catch (RuntimeIOException ex) {
            System.err.printf("Exception using ledPin %d\n", ledPin);
            ex.printStackTrace();
        }

        Diozero.shutdown(); // Will turn the led down if it is up.

        System.out.println("\nBye!");
    }
}
