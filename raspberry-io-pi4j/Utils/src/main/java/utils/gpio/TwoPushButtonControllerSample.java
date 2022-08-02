package utils.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Just test, to be run separately, for (remote) debugging...
 * 2 Buttons:
 * - One and Two pin on 3V3
 * - One pin on GPIO 28 (BCM 20, physical #38)
 * - Two pin on GPIO 29 (BCM 21, physical #40)
 */
public class TwoPushButtonControllerSample {

    private final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss.SSS");

    private static PushButtonController buttonOne;
    private static PushButtonController buttonTwo;

    private final Runnable sayHelloOne = () -> {
        try {
            System.out.println("+-----------------------+");
            System.out.printf("| OnClick [One]: Hello! (%s)%n", SDF.format(new Date()));
            System.out.println("+-----------------------+");
        } catch (Exception ex) {
            System.err.println("Say Hello:");
            ex.printStackTrace();
        }
    };
    private final Runnable sayHelloHelloOne = () -> {
        try {
            System.out.println("+-----------------------------------+");
            System.out.printf("| OnDoubleClick [One]: Hello Hello! (%s)%n", SDF.format(new Date()));
            System.out.println("+-----------------------------------+");
        } catch (Exception ex) {
            System.err.println("Say Hello Hello:");
            ex.printStackTrace();
        }
    };
    private final Runnable sayHelloooOne = () -> {
        try {
            System.out.println("+-----------------------------+");
            System.out.printf("| OnLongClick [One]: Hellooo! (%s)%n", SDF.format(new Date()));
            System.out.println("+-----------------------------+");
        } catch (Exception ex) {
            System.err.println("Say Hellooo:");
            ex.printStackTrace();
        }
    };
    private final Runnable sayHelloTwo = () -> {
        try {
            System.out.println("+-----------------------+");
            System.out.printf("| OnClick [Two]: Hello! (%s)%n", SDF.format(new Date()));
            System.out.println("+-----------------------+");
        } catch (Exception ex) {
            System.err.println("Say Hello:");
            ex.printStackTrace();
        }
    };
    private final Runnable sayHelloHelloTwo = () -> {
        try {
            System.out.println("+-----------------------------------+");
            System.out.printf("| OnDoubleClick [Two]: Hello Hello! (%s)%n", SDF.format(new Date()));
            System.out.println("+-----------------------------------+");
        } catch (Exception ex) {
            System.err.println("Say Hello Hello:");
            ex.printStackTrace();
        }
    };
    private final Runnable sayHelloooTwo = () -> {
        try {
            System.out.println("+-----------------------------+");
            System.out.printf("| OnLongClick [Two]: Hellooo! (%s)%n", SDF.format(new Date()));
            System.out.println("+-----------------------------+");
        } catch (Exception ex) {
            System.err.println("Say Hellooo:");
            ex.printStackTrace();
        }
    };

    public TwoPushButtonControllerSample() {
        try {
            // Provision buttons here
            Pin buttonOnePin = RaspiPin.GPIO_28; // wiPi 28, BCM 20, Physical #38.
            Pin buttonTwoPin = RaspiPin.GPIO_29; // wiPi 29, BCM 21, Physical #40.

            buttonOne = new PushButtonController(
                    "Button-One",
                    buttonOnePin,
                    sayHelloOne,
                    sayHelloHelloOne,
                    sayHelloooOne);

            if (false) {
                try { // This is a test... In case the failure we've seen comes from the two provisioning being too close to each other.
                    Thread.sleep(1_000L);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }

            buttonTwo = new PushButtonController(
                    "Button-Two",
                    buttonTwoPin,
                    sayHelloTwo,
                    sayHelloHelloTwo,
                    sayHelloooTwo);

        } catch (Throwable error) {
            error.printStackTrace();
        }
        System.out.println(">> Button 28 (physical #38) provisioned.");
        System.out.println(">> Button 29 (physical #40) provisioned.");
        System.out.println("\tTry click, double-click, long-click.");

    }

    public static void freeResources() {
        // Cleanup
        buttonOne.freeResources();
        buttonTwo.freeResources();
    }

    public static void main(String... args) {

        final Thread me = Thread.currentThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (me) {
                freeResources();
                me.notifyAll();
                try {
                    me.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "Shutdown Hook"));

        // System.setProperty("button.verbose", "true");

        new TwoPushButtonControllerSample();

        // Now wait for the user to stop the program
        System.out.println("Ctrl-C to stop.");
        try {
            synchronized (me) {
                me.wait();
                System.out.println("\nOk ok! Getting out.");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Bye!");
    }

}
