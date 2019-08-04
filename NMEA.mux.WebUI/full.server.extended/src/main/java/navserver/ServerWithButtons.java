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
import nmea.forwarders.SSD1306ProcessorI2C;
import utils.PinUtil;
import utils.TimeUtil;

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
			buttonOnePin = RaspiPin.GPIO_01;
			buttonTwoPin = RaspiPin.GPIO_02;

			try {
				// Identified by the PHYSICAL pin numbers
				String buttonOnePinStr = System.getProperty("buttonOne", "12"); // GPIO_01
				String buttonTwoPinStr = System.getProperty("buttonTwo", "13"); // GPIO_02

				buttonOnePin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonOnePinStr));
				buttonTwoPin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonTwoPinStr));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}

			// TODO Use the PushButtonMaster v2
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

		// Was the SSD1306 loaded?
		SSD1306ProcessorI2C oled = SSD1306ProcessorI2C.getInstance();
		if (oled == null) {
			System.out.println("SSD1306 was NOT loaded");
		} else {
			System.out.println("SSD1306 was loaded!");
			// Now let's write in the screen...
			TimeUtil.delay(20_000L);
			System.out.println("Taking ownership on the screen");
			oled.setExternallyOwned(true); // Taking ownership on the screen
			TimeUtil.delay(1_000L);
			oled.displayLines(new String[] { "Taking ownership", "on the screen"});
			TimeUtil.delay(1_000L);
			oled.displayLines(new String[] { "Releasing the screen"});
			TimeUtil.delay(500L);
			System.out.println("Releasing ownership on the screen");
			oled.setExternallyOwned(false); // Releasing ownership on the screen
		}
	}

	private void buttonOnePressed() {
		System.out.println("Button One pressed");
		boolean onOff = true;
		this.getMultiplexer().setEnableProcess(onOff); // ... for example
	}
	private void buttonOneReleased() {
		System.out.println("Button One released");

	}
	private void buttonTwoPressed() {
		System.out.println("Button Two pressed");
		this.getMultiplexer().stopAll(); // Another example...
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
