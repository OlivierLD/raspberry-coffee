package diozerotests;

import com.diozero.api.DeviceMode;
import com.diozero.api.PinInfo;
import com.diozero.devices.Button;
import com.diozero.devices.LED;
import com.diozero.internal.spi.NativeDeviceFactoryInterface;
import com.diozero.sampleapps.util.ConsoleUtil;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.SleepUtil;

import java.util.Map;

/*
 * GPIO Pin numbers, see https://www.diozero.com/api/gpio.html
 * PINs are BCM numbers.
 */

public class FirstTest {

    // PINs are BCM numbers.
    final static int ledPin    = 24; // 18 does not work on RPi 4B (4gb RAM) (it's an IN pin...), 24 does.
    final static int buttonPin = 12; // Seems OK.

    public static void main(String... args) {
        System.out.println("Starting diozero test.");

        // Check pins validity
        try (NativeDeviceFactoryInterface deviceFactory = DeviceFactoryHelper.getNativeDeviceFactory()) {
            final Map<String, Map<Integer, PinInfo>> headers = deviceFactory.getBoardInfo().getHeaders();

            // TODO Find pins ledPin and buttonPin better than with a full scan
            // We want DeviceMode.DIGITAL_OUTPUT for the led, and DeviceMode.DIGITAL_INPUT for the button

            System.out.println("----- Checking pins... ------");
            headers.entrySet().forEach(headerEntry -> {
                final Map<Integer, PinInfo> headerEntryValue = headerEntry.getValue();
                headerEntryValue.forEach((num, pinInfo) -> {
                    // final String modeString = ConsoleUtil.getModeString(deviceFactory, pinInfo);
                    // Do something
                    final int gpio = pinInfo.getDeviceNumber();
                    final DeviceMode gpioMode = deviceFactory.getGpioMode(gpio);
                    // We want DIGITAL_OUTPUT for the led
                    if (gpio == ledPin) {
                        if (!gpioMode.equals(DeviceMode.DIGITAL_OUTPUT)) {
                            System.err.printf("Led pin (%d) NOT suitable for output.\n", ledPin);
                        } else {
                            System.out.printf("Led pin (%d) is good to go.\n", ledPin);
                        }
                    }
                    // We want DIGITAL_INPUT for the button
                    if (gpio == buttonPin) {
                        if (!gpioMode.equals(DeviceMode.DIGITAL_INPUT)) {
                            System.err.printf("Button pin (%d) NOT suitable for input.\n", buttonPin);
                        } else {
                            System.out.printf("Button pin (%d) is good to go.\n", buttonPin);
                        }
                    }
                });
            });
            System.out.println("--- Done checking pins... ---");
        }

        // TODO See what happens if check above is not done.
        try (LED led = new LED(ledPin)) { // With Resource ;)
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
