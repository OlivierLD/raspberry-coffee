package diozerotests;

import com.diozero.devices.Button;
import com.diozero.devices.LED;
import com.diozero.util.SleepUtil;

/*
 * GPIO Pin numbers, see https://www.diozero.com/api/gpio.html
 * Pin numbers seem to be BCM numbers.
 * java -cp ./build/libs/common-utils-1.0-all.jar utils.PinUtil
 */

public class FirstTest {

    public static void main(String... args) {
        System.out.println("Starting diozero test.");
        try (LED led = new LED(18)) { // Pin numbers seem to be BCM numbers.
            System.out.println("Turning led 18 ON");
            led.on();
            SleepUtil.sleepSeconds(1);
            System.out.println("Turning led 18 OFF");
            led.off();
            SleepUtil.sleepSeconds(1);
            System.out.println("Toggling Led 18");
            led.toggle();
        }

        System.out.println("Button test... (20s)");
        try (Button button = new Button(12); LED led = new LED(18)) { // With resources, nice !
            button.whenPressed(nanoTime -> led.on());
            button.whenReleased(nanoTime -> led.off());
            SleepUtil.sleepSeconds(20);
        }
        System.out.println("Bye!");
    }
}
