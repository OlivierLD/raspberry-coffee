package diozerotests;

import com.diozero.devices.Button;
import com.diozero.devices.LED;
import com.diozero.util.SleepUtil;

/*
 * GPIO Pin numbers, see https://www.diozero.com/api/gpio.html
 * PINs are BCM numbers.
 * java -cp ./build/libs/common-utils-1.0-all.jar utils.PinUtil
 */

public class FirstTest {

    final static int ledPin    = 24; // 18 does not work (it's an IN pin...), 24 does.
    final static int buttonPin = 12; // Seems OK

    public static void main(String... args) {
        System.out.println("Starting diozero test.");
        try (LED led = new LED(ledPin)) { // PINs are BCM numbers.
            System.out.printf("Turning led %d ON\n", ledPin);
            led.on();
            SleepUtil.sleepSeconds(1);
            System.out.printf("Turning led %d OFF\n", ledPin);
            led.off();
            SleepUtil.sleepSeconds(1);
            System.out.printf("Toggling Led %d\n", ledPin);
            led.toggle();
        }

        System.out.println("Button test... (20s)");
        try (Button button = new Button(buttonPin); LED led = new LED(ledPin)) { // With resources, nice !
            button.whenPressed(nanoTime -> led.on());
            button.whenReleased(nanoTime -> led.off());
            SleepUtil.sleepSeconds(20);
        }
        System.out.println("Bye!");
    }
}
