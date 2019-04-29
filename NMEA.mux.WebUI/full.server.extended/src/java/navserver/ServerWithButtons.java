package navserver;

/**
 * Shows how to add push buttons to interact with the NavServer
 * Uses a small screen (oled SSD1306, Nokia, etc)
 */

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import navrest.NavServer;

public class ServerWithButtons extends NavServer {

	private static GpioController gpio = null;

	private Pin buttonOnePin;
	private Pin buttonTwoPin;

	private GpioPinDigitalInput buttonOne = null;
	private GpioPinDigitalInput buttonTwo = null;


	public ServerWithButtons() {

		super(); // NavServer

		try {
			gpio = GpioFactory.getInstance();
			// Provision buttons here
			buttonOnePin = RaspiPin.GPIO_01; // TODO From System property
			buttonTwoPin = RaspiPin.GPIO_02; // TODO From System property

			buttonOne = gpio.provisionDigitalInputPin(buttonOnePin, PinPullResistance.PULL_DOWN);
			buttonOne.addListener((GpioPinListenerDigital) event -> {
				if (event.getState().isHigh()) {
					buttonOnePressed();
				} else {
					buttonOneReleased();
				}
			});

			buttonTwo = gpio.provisionDigitalInputPin(buttonTwoPin, PinPullResistance.PULL_DOWN);
			buttonTwo.addListener((GpioPinListenerDigital) event -> {
				if (event.getState().isHigh()) {
					buttonTwoPressed();
				} else {
					buttonTwoReleased();
				}
			});
		} catch (Throwable error) {
			error.printStackTrace();
		}
	}

	private void buttonOnePressed() {
		System.out.println("Button One pressed");
		boolean onOff = true;
		this.getMultiplexer().setEnableProcess(onOff);
	}
	private void buttonOneReleased() {
		System.out.println("Button One released");

	}
	private void buttonTwoPressed() {
		System.out.println("Button Two pressed");
	}
	private void buttonTwoReleased() {
		System.out.println("Button Two released");

	}

	public static void freeResources() {
		if (gpio != null) {
			gpio.shutdown();
		}
	}


	public static void main(String... args) {

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			freeResources();
		}));
		new ServerWithButtons();
	}

}
