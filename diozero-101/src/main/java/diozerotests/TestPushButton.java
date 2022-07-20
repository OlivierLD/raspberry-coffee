package diozerotests;

import com.diozero.api.GpioPullUpDown;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.Button;
import com.diozero.devices.LED;
import com.diozero.util.Diozero;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * GPIO Pin numbers, see https://www.diozero.com/api/gpio.html
 * PINs are BCM numbers.
 */

public class TestPushButton {

    // PINs are BCM numbers.
    static int ledPin    = 18; // 8 does not work on RPi 4B (4gb RAM) (even if it is an OUT pin...), 18, 22 do.
    static int buttonPin = 12; // Seems OK.

    private final static boolean BUTTON_VERBOSE = "true".equals(System.getProperty("button-verbose"));

    private final static String LED_PIN_PREFIX = "--led-pin:";
    private final static String BUTTON_PIN_PREFIX = "--button-pin:";
    /**
     * Use the -Dcheck-pins=true to check pins directions, -Dbutton-verbose=true
     * @param args Optional --led-pin:XX --button-pin:XX (XX: BCM numbers)
     */
    public static void main(String... args) {
        System.out.println("Starting diozero test.");

        Arrays.stream(args).forEach(arg -> {
            if (arg.startsWith(LED_PIN_PREFIX)) {
                ledPin = Integer.parseInt(arg.substring(LED_PIN_PREFIX.length()));
            } else if (arg.startsWith(BUTTON_PIN_PREFIX)) {
                buttonPin = Integer.parseInt(arg.substring(BUTTON_PIN_PREFIX.length()));
            } else {
                System.out.printf("Un-managed prefix %s\n", arg);
            }
        });

        System.out.printf("WIll use ledPin %d, buttonPin %d\n.", ledPin, buttonPin);

        System.out.println("Button test... (20s)");

        final Thread currentThread = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (currentThread) {
                currentThread.notify();
            }
        }, "Oops"));

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

            // SleepUtil.sleepSeconds(20); // in seconds
            synchronized (currentThread) {
                try {
                    currentThread.wait();
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }

        } catch (RuntimeIOException ex) {
            System.err.printf("Exception using ledPin %d, buttonPin %d\n", ledPin, buttonPin);
            ex.printStackTrace();
        }
        System.out.println("Done with the button.");

        Diozero.shutdown();

        System.out.println("Bye!");
    }
}
