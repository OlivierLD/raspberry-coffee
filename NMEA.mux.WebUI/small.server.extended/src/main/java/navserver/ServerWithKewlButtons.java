package navserver;

/**
 * Shows how to add push buttons to interact with the NavServer
 * Buttons with click, double-click, long-click, and other combinations.
 * Uses a small screen (oled SSD1306, Nokia, etc)
 */

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import navrest.NavServer;
import navserver.button.PushButtonMaster;
import nmea.forwarders.SSD1306Processor;
import utils.TimeUtil;

public class ServerWithKewlButtons extends NavServer {

	private Pin appPin;
	private Pin shiftPin;

	final static PushButtonMaster pbmOne = new PushButtonMaster();
	final static PushButtonMaster pbmShift = new PushButtonMaster();

	// Action to take depending on the type of click.
	// TODO Propagate the button events to the SSD1306Processor (simple clicks, up and down)
	Runnable onClick = () -> {
		System.out.println(String.format(">> %sSingle click", (pbmShift.isPushed() ? "[Shft] + " : "")));
	};
	Runnable onDoubleClick = () -> {
		System.out.println(String.format(">> %sDouble click", (pbmShift.isPushed() ? "[Shft] + " : "")));
	};
	Runnable onLongClick = () -> {
		System.out.println(String.format(">> %sLong click", (pbmShift.isPushed() ? "[Shft] + " : "")));
	};
	/**
	 *  For the Shift button, no operation needed. We only need if it is up or down.
	 *  See {@link PushButtonMaster#isPushed()}
	 */

	public ServerWithKewlButtons() {

		super(); // NavServer

		try {
			// Provision buttons here
			appPin = RaspiPin.GPIO_01;
			shiftPin = RaspiPin.GPIO_02;

			// Change pins, based on system properties
//			try {
//				// Identified by the PHYSICAL pin numbers
//				String buttonOnePinStr = System.getProperty("buttonOne", "12"); // GPIO_01
//				String buttonTwoPinStr = System.getProperty("buttonTwo", "13"); // GPIO_02
//
//				buttonOnePin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonOnePinStr));
//				buttonTwoPin = PinUtil.getPinByPhysicalNumber(Integer.parseInt(buttonTwoPinStr));
//			} catch (NumberFormatException nfe) {
//				nfe.printStackTrace();
//			}

			pbmOne.update(
					"App-Button",
					appPin,
					onClick,
					onDoubleClick,
					onLongClick);

			pbmShift.update(
					"Shift-Button",
					shiftPin);

		} catch (Throwable error) {
			error.printStackTrace();
		}

		// Was the SSD1306 loaded? This is loaded by the properties file.
		// Use the SSD1306Processor, SPI version.
		SSD1306Processor oled = SSD1306Processor.getInstance();
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

	public static void freeResources() {
		// Cleanup
		pbmOne.freeResources();
		pbmShift.freeResources();
	}


	public static void main(String... args) {

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			freeResources();
		}));
		new ServerWithKewlButtons();
	}

}
