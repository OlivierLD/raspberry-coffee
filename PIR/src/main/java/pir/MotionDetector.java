package pir;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/*
 * PIR: PyroElectric Infra Red
 * Motion Sensor
 */
public class MotionDetector {
	final GpioController gpio = GpioFactory.getInstance();
	private static final Pin DEFAULT_PIN = RaspiPin.GPIO_01; // #12, GPIO 18.
	private Pin pirPin = null;
	private GpioPinDigitalInput pirInput;
	private MotionDetectionInterface detector;

	public MotionDetector(MotionDetectionInterface parent) {
		this(DEFAULT_PIN, parent);
	}

	public MotionDetector(Pin p, MotionDetectionInterface parent) {
		this.pirPin = p;
		this.detector = parent;
		init();
	}

	private void init() {
		this.pirInput = gpio.provisionDigitalInputPin(pirPin, "Motion");
		this.pirInput.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if ("true".equals(System.getProperty("pir.verbose", "false")))
					System.out.println(" >>> GPIO pin state changed: time=" + System.currentTimeMillis() + ", " + event.getPin() + " = " + event.getState());
				if (event.getState().isHigh()) {
					detector.motionDetected();
				}
			}
		});
	}

	public void shutdown() {
		gpio.shutdown();
	}
}
