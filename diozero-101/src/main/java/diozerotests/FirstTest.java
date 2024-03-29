package diozerotests;

import com.diozero.api.DeviceMode;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.Button;
import com.diozero.devices.LED;
import com.diozero.internal.spi.NativeDeviceFactoryInterface;
// import com.diozero.sampleapps.util.ConsoleUtil;
import com.diozero.sbc.DeviceFactoryHelper;
import com.diozero.util.Diozero;
import com.diozero.util.SleepUtil;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * GPIO Pin numbers, see https://www.diozero.com/api/gpio.html
 * PINs are BCM numbers.
 */

public class FirstTest {

    // PINs are BCM numbers.
    static int ledPin    = 18; // 8 does not work on RPi 4B (4gb RAM) (even if it is an OUT pin...), 18, 22 do.
    static int buttonPin = 12; // Seems OK.

    private final static boolean CHECK_PINS = "true".equals(System.getProperty("check-pins"));
    private final static boolean BUTTON_VERBOSE = "true".equals(System.getProperty("button-verbose"));

    private final static String LED_PIN_PREFIX = "--led-pin:";
    private final static String BUTTON_PIN_PREFIX = "--button-pin:";
    /**
     * Use the -Dcheck-pins=true to check pins directions, -Dbutton-verbose=true
     * @param args Optional --led-pin:XX --button-pin:XX (XX: BCM numbers)
     */
    public static void main(String... args) {
        System.out.printf("Starting diozero test %s.\n", FirstTest.class.getName());

        Arrays.stream(args).forEach(arg -> {
            if (arg.startsWith(LED_PIN_PREFIX)) {
                ledPin = Integer.parseInt(arg.substring(LED_PIN_PREFIX.length()));
            } else if (arg.startsWith(BUTTON_PIN_PREFIX)) {
                buttonPin = Integer.parseInt(arg.substring(BUTTON_PIN_PREFIX.length()));
            } else {
                System.out.printf("Un-managed prefix %s\n", arg);
            }
        });

        System.out.printf("Will use ledPin %d, buttonPin %d.\n", ledPin, buttonPin);

        // Check pins validity
        if (CHECK_PINS) {
            try (NativeDeviceFactoryInterface deviceFactory = DeviceFactoryHelper.getNativeDeviceFactory()) {
                final Map<String, Map<Integer, PinInfo>> headers = deviceFactory.getBoardInfo().getHeaders();

                // TODO Find pins ledPin and buttonPin better than with a full scan.
                // We want DeviceMode.DIGITAL_OUTPUT for the led, and DeviceMode.DIGITAL_INPUT for the button

                System.out.println("----- Checking pins... ------");
//                headers.entrySet().forEach(headerEntry -> {
//                    final Map<Integer, PinInfo> headerEntryValue = headerEntry.getValue();
                headers.forEach( (key, headerEntryValue) -> headerEntryValue.forEach((num, pinInfo) -> {
                    // final String modeString = ConsoleUtil.getModeString(deviceFactory, pinInfo);
                    // Do something
                    final int gpio = pinInfo.getDeviceNumber();
                    final DeviceMode gpioMode = deviceFactory.getGpioMode(gpio);
                    // We want DIGITAL_OUTPUT for the LED
                    if (gpio == ledPin) {
                        if (!gpioMode.equals(DeviceMode.DIGITAL_OUTPUT)) {
                            System.err.printf("Led pin (%d, %s) NOT suitable for output.\n", ledPin, pinInfo.getName());
                        } else {
                            System.out.printf("Led pin (%d, %s) is good to go.\n", ledPin, pinInfo.getName());
                        }
                    }
                    // We want DIGITAL_INPUT for the button
                    if (gpio == buttonPin) {
                        if (!gpioMode.equals(DeviceMode.DIGITAL_INPUT)) {
                            System.err.printf("Button pin (%d, %s) NOT suitable for input.\n", buttonPin, pinInfo.getName());
                        } else {
                            System.out.printf("Button pin (%d, %s) is good to go.\n", buttonPin, pinInfo.getName());
                        }
                    }
                }));
                System.out.println("--- Done checking pins... ---");
            } finally {
                Diozero.shutdown();
            }
        }

        try (LED led = new LED(ledPin)) { // With Resource ;)
            System.out.printf("Turning led %d ON\n", ledPin);
            led.on();
            SleepUtil.sleepSeconds(2);
            System.out.printf("Turning led %d OFF\n", ledPin);
            led.off();
            SleepUtil.sleepSeconds(2);
            System.out.printf("Toggling Led %d\n", ledPin);
            led.toggle(); // Back on
            SleepUtil.sleepSeconds(2);
            System.out.printf("Toggling Led %d\n", ledPin);
            led.toggle(); // Back off
            SleepUtil.sleepSeconds(2);
        } catch (RuntimeIOException ex) {
            System.err.printf("Exception using ledPin %d\n", ledPin);
            ex.printStackTrace();
        }

        System.out.println("Button test... (20s)");
        AtomicBoolean buttonPressed = new AtomicBoolean(false); // false: led is (should be) off.
        try (Button button = new Button(buttonPin, GpioPullUpDown.PULL_UP); LED led = new LED(ledPin)) { // With resources, nice !

            System.out.println("--- Button block, top.");
//            led.off();
//            SleepUtil.sleepSeconds(2);

            // Important: See the GpioPullUpDown.PULL_UP in the new Button()... See ButtonControlledLed in the sample apps.
            // If not set, pushed and released are inverted !!

            button.whenPressed(nanoTime -> {
                if (BUTTON_VERBOSE && !buttonPressed.get()) {
                    // TODO What is that nanoTime thing ?... See https://stackoverflow.com/questions/3523442/difference-between-clock-realtime-and-clock-monotonic
                    System.out.printf("Button pressed, turning led on (at nanoTime: %s, %s)\n", NumberFormat.getInstance().format(nanoTime), new Date(nanoTime / 1_000 / 1_000));
                    buttonPressed.set(true);
                }
                led.on();
            });
            button.whenReleased(nanoTime -> {
                if (BUTTON_VERBOSE && buttonPressed.get()) {
                    // TODO What is that nanoTime thing ?...
                    System.out.printf("Button released, turning led off (at nanoTime: %s, %s)\n", NumberFormat.getInstance().format(nanoTime), new Date(nanoTime / 1_000 / 1_000));
                    buttonPressed.set(false);
                }
                led.off();
            });
            led.off();
            System.out.println("--- Button block, waiting 20s.");

            SleepUtil.sleepSeconds(20); // in seconds

        } catch (RuntimeIOException ex) {
            System.err.printf("Exception using ledPin %d, buttonPin %d\n", ledPin, buttonPin);
            ex.printStackTrace();
        }
        System.out.println("Done with the button.");

        Diozero.shutdown();

        System.out.println("Bye!");
    }
}
