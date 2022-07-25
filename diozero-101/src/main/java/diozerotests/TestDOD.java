package diozerotests;

import com.diozero.api.DigitalOutputDevice;
import com.diozero.api.RuntimeIOException;
import com.diozero.util.SleepUtil;

/**
 * DOD stands for Digital Output Device
 * We will use it for a LED (Note: LED extends DigitalOutputDevice)
 */
public class TestDOD {

    public static void main(String... args) {

        final int gpioPin = 128;
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
