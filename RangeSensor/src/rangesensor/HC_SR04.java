package rangesensor;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import utils.TimeUtil;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;

/**
 * See https://www.modmypi.com/blog/hc-sr04-ultrasonic-range-sensor-on-the-raspberry-pi
 */
public class HC_SR04 {
	private final static Format DF22 = new DecimalFormat("#0.00");
	private final static Format DF_N = new DecimalFormat("#.##########################");

	private final static double SOUND_SPEED = 34_029d;       // in cm, 340.29 m/s
	private final static double DIST_FACT = SOUND_SPEED / 2; // round trip
	private final static int MIN_DIST = 3; // in cm

	private static boolean verbose = "true".equals(System.getProperty("hc_sr04.verbose"));
	private final static long BILLION = (long) 1E9;
	private final static int TEN_MICRO_SEC = 10_000; // In Nano secs

	private GpioController gpio;

	private GpioPinDigitalOutput trigPin;
	private GpioPinDigitalInput echoPin;

	public HC_SR04() {
		this(RaspiPin.GPIO_04, RaspiPin.GPIO_05);
	}

	public HC_SR04(Pin trig, Pin echo) {
		init(trig, echo);
	}
	private void init(Pin trig, Pin echo) {
		// create gpio controller
		gpio = GpioFactory.getInstance();
		// 2 pins
		trigPin = gpio.provisionDigitalOutputPin(trig, "Trig", PinState.LOW);
		echoPin = gpio.provisionDigitalInputPin(echo, "Echo");
	}

	public void stop() {
		trigPin.low(); // Off
		gpio.shutdown();
	}

	public Pin getTrigPin() {
		return this.trigPin.getPin();
	}

	public Pin getEchoPin() {
		return this.echoPin.getPin();
	}

	private final static long MAX_WAIT = 100; // 100ms = 1/10 of sec.
	private static boolean tooLong(long startedAt) {
		if ((System.currentTimeMillis() - startedAt) < MAX_WAIT) {
			return false;
		} else {
			if (verbose) {
				System.out.println("Echo took too long!!");
			}
			return true;
		}
	}

	public double readDistance() {
		double distance = -1L;
		trigPin.low();
//	TimeUtil.delay(500L);

		// Just to check...
		if (echoPin.isHigh()) {
			System.out.println(">>> !! Before sending signal, echo PIN is " + (echoPin.isHigh() ? "High" : "Low"));
		}
		trigPin.high();
		// 10 microsec to trigger the module  (8 ultrasound bursts at 40 kHz)
		// https://www.dropbox.com/s/615w1321sg9epjj/hc-sr04-ultrasound-timing-diagram.png
		TimeUtil.delay(0, TEN_MICRO_SEC);
		trigPin.low();

		// Wait for the signal to return
		long now = System.currentTimeMillis();
		while (echoPin.isLow() && !tooLong(now)); // && (start == 0 || (start != 0 && (start - top) < BILLION)))
		long start = System.nanoTime();
		// There it is, the echo comes back.
		now = System.currentTimeMillis();
		while (echoPin.isHigh() && !tooLong(now));
		long end = System.nanoTime();

		//  System.out.println(">>> TOP: start=" + start + ", end=" + end);
		//  System.out.println("Nb Low Check:" + nbLowCheck + ", Nb High Check:" + nbHighCheck);

		if (end > start) { //  && start > 0)
			double pulseDuration = (double) (end - start) / (double) BILLION; // in seconds
//      System.out.println("Duration:" + (end - start) + " nanoS"); // DF_N.format(pulseDuration));
			distance = pulseDuration * DIST_FACT;
			if (verbose) {
				if (distance < 1_000) { // Less than 10 meters
					System.out.println("Distance: " + DF22.format(distance) + " cm. (" + distance + "), Duration:" + NumberFormat.getInstance().format(end - start) + " nanoS"); // + " (" + pulseDuration + " = " + end + " - " + start + ")");
				} else {
					System.out.println("   >>> Too far:" + DF22.format(distance) + " cm.");
				}
			}
		} else {
			throw new RuntimeException("Hiccup! start:" + NumberFormat.getInstance().format(start) + ", end:" + NumberFormat.getInstance().format(end));
		}
		return distance;
	}

	public static void main(String... args) {

//	System.out.println("BILLION:" + NumberFormat.getInstance().format(BILLION));

		System.out.println("GPIO Control - Range Sensor HC-SR04.");
		System.out.println("Will stop is distance is smaller than " + MIN_DIST + " cm");

		verbose = "true".equals(System.getProperty("verbose", "false"));

		HC_SR04 hcSR04 = new HC_SR04();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nOops!");
			hcSR04.stop();
			System.out.println("Exiting nicely.");
		}));

		System.out.println(">>> Waiting for the sensor to be ready (2s)...");
		TimeUtil.delay(2_000L);

		boolean go = true;
		System.out.println("Looping until the distance is less than " + MIN_DIST + " cm");
		while (go) {
			double distance = 0;
			try {
				distance = hcSR04.readDistance();
			} catch (Exception ex) {
				System.out.println(ex.toString());
				TimeUtil.delay(500L);
			}
			if (distance > 0 && distance < MIN_DIST) {
				go = false;
			} else {
				if (distance < 0 && verbose) {
					System.out.println("Dist:" + distance);
				}
				try {
					TimeUtil.delay(1_000L);
				} catch (Exception ex) {
				}
			}
		}
		System.out.println("Done.");
		hcSR04.stop();
	}
}
