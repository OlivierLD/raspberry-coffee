package nmea.forwarders;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.util.Properties;
import nmea.parser.StringParsers;

/**
 * This is a {@link Forwarder}, blinking a led every time a valid message is received.
 * <br>
 * It can be loaded dynamically. From the properties file used at startup, or the Web UI.
 * <br>
 * To load it, use the properties file at startup:
 * <pre>
 *   forward.XX.cls=nmea.forwarders.LedBlinker
 * </pre>
 * A jar containing this class and its dependencies must be available in the classpath.
 *
 * Wiring:
 * - led + (long leg) on RPi GPIO_01 (pin #12) with a 220 Ohms resistor
 * - led - (short leg) on RPi GND (pin #6)
 */
public class LedBlinker implements Forwarder {

	private GpioController gpio;
	private GpioPinDigitalOutput pin;
	/*
	 * @throws Exception
	 */
	public LedBlinker() throws Exception {
		try {
			gpio = GpioFactory.getInstance();
			pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "BlinkingLED", PinState.LOW);
		} catch (Throwable t) {
			gpio = null;
			pin = null;
			throw new RuntimeException(t);
		}
	}

	@Override
	public void write(byte[] message) {
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);
		if (StringParsers.validCheckSum(str)) {
			// OK
			if (pin != null) {
				pin.pulse(1, true); // set second argument to 'true' use a blocking call
			}
		} else {
			// Not OK
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop blinking with " + this.getClass().getName());
		if (gpio != null) {
			gpio.shutdown();
		}
	}

	public static class LedBlinkerBean {
		private String cls;
		private String type = "led-blinker";

		public LedBlinkerBean(LedBlinker instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new LedBlinkerBean(this);
	}

	@Override
	public void setProperties(Properties props) {
		// This could be used to change the pin number...
	}
}
