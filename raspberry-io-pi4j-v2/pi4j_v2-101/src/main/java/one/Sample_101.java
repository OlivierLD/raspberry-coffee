package one;

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.platform.Platforms;
import com.pi4j.util.Console;

/*
 * From https://pi4j.com/getting-started/minimal-example-application/
 */

public class Sample_101 {

    private static int pressCount = 0;
    // PI4J v2 uses BCM numbers.
    private static final int PIN_BUTTON = 24; // PIN #18 = BCM 24
    private static final int PIN_LED = 22;    // PIN #15 = BCM 22

    public static void main(String... args) throws InterruptedException {
        var pi4j = Pi4J.newAutoContext();

        Platforms platforms = pi4j.platforms();

        Console console = new Console();
        console.box("Pi4J PLATFORMS");
        console.println();
        platforms.describe().print(System.out);
        console.println();

        var buttonConfig = DigitalInput.newConfigBuilder(pi4j)
                .id("button")
                .name("Press button")
                .address(PIN_BUTTON)
                .pull(PullResistance.PULL_DOWN)
                .debounce(3_000L)
                .provider("pigpio-digital-input"); // Problem with that one ?...

        var button = pi4j.create(buttonConfig);

        button.addListener(e -> {
            if (e.state() == DigitalState.LOW) {
                pressCount++;
                console.println("Button was pressed for the " + pressCount + "th time");
            }
        });

        var ledConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("LED Flasher")
                .address(PIN_LED)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("pigpio-digital-output");

        var led = pi4j.create(ledConfig);

        while (pressCount < 5) {
            if (led.equals(DigitalState.HIGH)) {
                led.low();
            } else {
                led.high();
            }
            Thread.sleep(500 / (pressCount + 1));
        }
        pi4j.shutdown();
    }
}
