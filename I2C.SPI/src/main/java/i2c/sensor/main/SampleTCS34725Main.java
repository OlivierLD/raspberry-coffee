package i2c.sensor.main;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import i2c.sensor.TCS34725;

public class SampleTCS34725Main {
	private static boolean go = true;

	public static void main(String... args) throws Exception {
		int colorThreshold = 4_000;
		if (args.length > 0) {
			try {
				colorThreshold = Integer.parseInt(args[0]);
			} catch (NumberFormatException nfe) {
				System.err.println(nfe.toString());
			}
		}
		final TCS34725 sensor = new TCS34725(TCS34725.TCS34725_INTEGRATIONTIME_50MS, TCS34725.TCS34725_GAIN_4X);
		// Setup output pins here for the 3 color led
		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalOutput greenPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "green", PinState.LOW);
		final GpioPinDigitalOutput bluePin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "blue", PinState.LOW);
		final GpioPinDigitalOutput redPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "red", PinState.LOW);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			System.out.println("\nBye");
		}));
		// Main loop
		while (go) {
			sensor.setInterrupt(false); // turn led on
			try {
				Thread.sleep(60);
			} catch (InterruptedException ie) {
			} // Takes 50ms to read, see above
			TCS34725.TCSColor color = sensor.getRawData();
			sensor.setInterrupt(true); // turn led off
			int r = color.getR(),
					g = color.getG(),
					b = color.getB();
			// Display the color on the 3-color led accordingly
			System.out.println("Read color R:" + r +
					" G:" + g +
					" B:" + b);
			// Send to 3-color led. The output is digital!! Not analog.
			// Use a DAC: https://learn.adafruit.com/mcp4725-12-bit-dac-with-raspberry-pi/overview
			// For now, take the biggest one
			if (r > colorThreshold || g > colorThreshold || b > colorThreshold) {
				int max = Math.max(r, g);
				max = Math.max(max, b);
				if (max == r) {
					System.out.println("Red!");
					redPin.high();
				} else
					redPin.low();
				if (max == g) {
					System.out.println("Green!");
					greenPin.high();
				} else
					greenPin.low();
				if (max == b) {
					System.out.println("Blue!");
					bluePin.high();
				} else
					bluePin.low();
			} else {
				redPin.low();
				greenPin.low();
				bluePin.low();
			}
		}
		redPin.low();
		greenPin.low();
		bluePin.low();
		gpio.shutdown();
		System.out.println("Exiting. Thanks.");
	}
}
