package diozerotests;

import com.diozero.devices.Button;
import com.diozero.devices.LED;
import com.diozero.util.SleepUtil;

public class FirstTest {

    public static void main(String... args) {

        try (LED led = new LED(18)) {
            led.on();
            SleepUtil.sleepSeconds(1);
            led.off();
            SleepUtil.sleepSeconds(1);
            led.toggle();
        }

        try (Button button = new Button(12); LED led = new LED(18)) {
            button.whenPressed(nanoTime -> led.on());
            button.whenReleased(nanoTime -> led.off());
            SleepUtil.sleepSeconds(20);
        }

    }
}
