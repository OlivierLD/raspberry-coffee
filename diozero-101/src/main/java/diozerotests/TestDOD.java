package diozerotests;

import com.diozero.api.DigitalOutputDevice;
import com.diozero.api.RuntimeIOException;
import com.diozero.util.SleepUtil;

import java.util.Arrays;

/**
 * DOD stands for Digital Output Device
 * We will use it for a LED (Note: LED extends DigitalOutputDevice).
 *
 * Note: This is also suitable for a Relay.
 * See ../RESTRelay/relay_bb.png
 *
 * Compare to its PI4J equivalent, gpio01.GPIO01led
 */
public class TestDOD {

    // PINs are BCM numbers.
    static int gpioPin    = 18;

    private final static String GPIO_PIN_PREFIX = "--gpio-pin:"; // CLI parameter

    /**
     * @param args Optional --gpio-pin:XX (XX: BCM numbers)
     */
    public static void main(String... args) {

        System.out.printf("Starting diozero test %s.\n", TestToggleLed.class.getName());

        Arrays.stream(args).forEach(arg -> {
            if (arg.startsWith(GPIO_PIN_PREFIX)) {
                gpioPin = Integer.parseInt(arg.substring(GPIO_PIN_PREFIX.length()));
            } else {
                System.out.printf("Un-managed prefix %s\n", arg);
            }
        });
        System.out.printf("Will use ledPin %d.\n", gpioPin);

        try (DigitalOutputDevice dod = new DigitalOutputDevice(gpioPin, true, false)) {
            System.out.printf("Turning device %d ON\n", gpioPin);
            dod.on();
            SleepUtil.sleepSeconds(2);
            System.out.printf("Turning device %d OFF\n", gpioPin);
            dod.off();
            SleepUtil.sleepSeconds(2);
            System.out.printf("Toggling device %d\n", gpioPin);
            dod.toggle(); // Back on
            SleepUtil.sleepSeconds(2);
            System.out.printf("Toggling device %d\n", gpioPin);
            dod.toggle(); // Back off
            SleepUtil.sleepSeconds(2);
        } catch (RuntimeIOException ex) {
            System.err.printf("Exception using device pin %d\n", gpioPin);
            ex.printStackTrace();
        }
        System.out.println("Bye now.");
    }
}
