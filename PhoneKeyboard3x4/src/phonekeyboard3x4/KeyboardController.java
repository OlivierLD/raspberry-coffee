package phonekeyboard3x4;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

public class KeyboardController {
	private GpioController gpio = null;
	private GpioPinDigitalMultipurpose[] rowButton = null;
	private GpioPinDigitalMultipurpose[] colButton = null;

	private final static char[][] keypad = new char[][]
			{
					{'1', '2', '3'},
					{'4', '5', '6'},
					{'7', '8', '9'},
					{'*', '0', '#'}
			};

	/*
	 * Seen from the TOP of the keypad, the 8 pins
	 * https://www.adafruit.com/products/1824
	 *     +-----+ +-----+ +-----+
	 *     |  3  | |  2  | |  1  |   <- The keys you see
	 *  +--+-----+-+-----+-+-----+--+
	 *  |          T  O  P          |
	 * -+--+--+--+--+--+--+--+--+---+-
	 *     |  |  |  |  |  |  |  |
	 *     x  25 24 23 18 22 17 4    <- Pin names on the cobbler
	 *        |  |  |  |  |  |  |
	 *        |  |  |  |  |  |  Col [1,4,7,*]
	 *        |  |  |  |  |  Col [2,5,8,0]
	 *        |  |  |  |  Col [3,6,9,#]
	 *        |  |  |  Row [1,2,3]
	 *        |  |  Row [4,5,6]
	 *        |  Row [7,8,9]
	 *        Row [*,0,#]
	 */

	// TODO parameterize those 2 arrays (customizable)
	// Names on the cobbler ->                    18                23                24                25
	private Pin[] kpRow = new Pin[]{RaspiPin.GPIO_01, RaspiPin.GPIO_04, RaspiPin.GPIO_05, RaspiPin.GPIO_06}; // Wiring PI/PI4J
	// Names on the cobbler ->                     4                17                22
	private Pin[] kpCol = new Pin[]{RaspiPin.GPIO_07, RaspiPin.GPIO_00, RaspiPin.GPIO_03};                   // Wiring PI/PI4J

	public KeyboardController() {
		this.gpio = GpioFactory.getInstance();
		rowButton = new GpioPinDigitalMultipurpose[kpRow.length];
		for (int i = 0; i < kpRow.length; i++) {
			rowButton[i] = this.gpio.provisionDigitalMultipurposePin(kpRow[i], PinMode.DIGITAL_INPUT);
		}
		colButton = new GpioPinDigitalMultipurpose[kpCol.length];
		for (int i = 0; i < kpCol.length; i++) {
			colButton[i] = this.gpio.provisionDigitalMultipurposePin(kpCol[i], PinMode.DIGITAL_INPUT);
		}
		if ("y".equalsIgnoreCase(System.getProperty("verbose", "N"))) {
			System.out.println("All pins provisioned, keypad ready.");
		}
	}

	public char getKey() {
		char c = ' ';
		while (c == ' ') {
			// Set all column to output low
			for (int i = 0; i < colButton.length; i++) {
				colButton[i].setMode(PinMode.DIGITAL_OUTPUT);
				colButton[i].low();
			}

			// All rows as input
			for (int i = 0; i < rowButton.length; i++) {
				rowButton[i].setMode(PinMode.DIGITAL_INPUT);
				rowButton[i].setPullResistance(PinPullResistance.PULL_UP);
			}
			int row = -1, col = -1;
			// Scan rows for pushed keys
			for (int i = 0; i < rowButton.length; i++) {
				if (this.gpio.isLow(rowButton[i])) {
					row = i;
					break;
				}
			}
			if (row != -1) {
				// Set (convert) columns to input
				for (int i = 0; i < colButton.length; i++) {
					colButton[i].setMode(PinMode.DIGITAL_INPUT);
					colButton[i].setPullResistance(PinPullResistance.PULL_DOWN);
				}
				// Scan cols for pushed keys
				for (int i = 0; i < kpCol.length; i++) {
					if (this.gpio.isHigh(colButton[i])) {
						col = i;
						break;
					}
				}
				if (col != -1) {
					c = keypad[row][col];
					reset();
					//   System.out.println(" >>> getKey: [" + row + ", " + col + "] = " + c);
				} else {
					reset();
				}
			} else {
				reset();
			}
		}
		return c;
	}

	/**
	 * Reinitialize all rows and columns as input at exit
	 */
	private void reset() {
		if (rowButton != null) {
			for (int i = 0; i < rowButton.length; i++) {
				rowButton[i].setMode(PinMode.DIGITAL_INPUT);
				rowButton[i].setPullResistance(PinPullResistance.PULL_UP);
			}
		}
		if (colButton != null) {
			for (int i = 0; i < colButton.length; i++) {
				colButton[i].setMode(PinMode.DIGITAL_INPUT);
				colButton[i].setPullResistance(PinPullResistance.PULL_UP);
			}
		}
	}

	public void shutdown() {
		this.gpio.shutdown();
	}
}
