package phonekeyboard3x4;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import utils.PinUtil;
import utils.TimeUtil;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

/**
 * Good explanation here: https://www.youtube.com/watch?v=yYnX5QodqQ4
 *
 */
public class KeyboardController {

	private boolean verbose = false;

	private GpioController gpio = null;
	private GpioPinDigitalMultipurpose[] rowButton = null;
	private GpioPinDigitalMultipurpose[] colButton = null;

	private final static char[][] keypad = new char[][] {
					{'1', '2', '3'},
					{'4', '5', '6'},
					{'7', '8', '9'},
					{'*', '0', '#'}
			};

	/*
	 * Seen from the TOP of the keypad, the 8 pins (one is unused).
	 * Designed fo the keypad at https://www.adafruit.com/products/1824
	 *
	 * For the https://www.adafruit.com/product/419 3x4 keypad, see Fritzing diagram.
	 *
	 *     +-----+ +-----+ +-----+
	 *     |  3  | |  2  | |  1  |   <- The keys you see
	 *  +--+-----+-+-----+-+-----+--+
	 *  |          T  O  P          |
	 * -+--+--+--+--+--+--+--+--+---+-
	 *     |  |  |  |  |  |  |  |
	 *     x  25 24 23 18 22 17 4    <- Pin names on the cobbler (col BCM in PinUtil)
	 *        |  |  |  |  |  |  |
	 *        |  |  |  |  |  |  Col [1,4,7,*]
	 *        |  |  |  |  |  Col [2,5,8,0]
	 *        |  |  |  |  Col [3,6,9,#]
	 *        |  |  |  Row [1,2,3]
	 *        |  |  Row [4,5,6]
	 *        |  Row [7,8,9]
	 *        Row [*,0,#]
	 */

	// 2 arrays, customizable through system properties
	private Pin[] kpRow = new Pin[] {
		PinUtil.GPIOPin.GPIO_1.pin(),
		PinUtil.GPIOPin.GPIO_4.pin(),
		PinUtil.GPIOPin.GPIO_5.pin(),
		PinUtil.GPIOPin.GPIO_6.pin()
	};
	private Pin[] kpCol = new Pin[] {
		PinUtil.GPIOPin.GPIO_7.pin(),
		PinUtil.GPIOPin.GPIO_0.pin(),
		PinUtil.GPIOPin.GPIO_3.pin()
	};

	public KeyboardController() {
		this(false);
	}
	public KeyboardController(boolean print) {

		this.verbose = "true".equals(System.getProperty("keypad.verbose"));

		// Default -Dkeypad.rows=GPIO_1,GPIO_4,GPIO_5,GPIO_6
		String userProvidedRows = System.getProperty("keypad.rows");
		// Default -Dkeypad.cols=GPIO_7,GPIO_0,GPIO_3
		String userProvidedCols = System.getProperty("keypad.cols");
		if (userProvidedCols != null || userProvidedRows != null) {
			if (userProvidedCols == null || userProvidedRows == null) {
				throw new InvalidParameterException("Please provide both keypad.rows AND keypad.cols, or none");
			} else {
				String[] userRows = userProvidedRows.split(",");
				String[] userCols = userProvidedCols.split(",");
				if (userRows.length != 4) {
					throw new InvalidParameterException("keypad.rows should contain 4 elements, comma-separated.");
				}
				if (userCols.length != 3) {
					throw new InvalidParameterException("keypad.cols should contain 3 elements, comma-separated.");
				}
				// Check unicity
				for (int i=0; i<userRows.length; i++) {
					for (int j=i+1; j<userRows.length; j++) {
						if (userRows[i].trim().equals(userRows[j].trim())) {
							throw new InvalidParameterException(String.format("[%s] cannot appear more than once", userRows[i]));
						}
					}
					for (int j=0; j<userCols.length; j++) {
						if (userRows[i].trim().equals(userCols[j].trim())) {
							throw new InvalidParameterException(String.format("[%s] cannot appear more than once", userRows[i]));
						}
					}
				}
				for (int i=0; i<userCols.length; i++) {
					for (int j = i + 1; j < userCols.length; j++) {
						if (userCols[i].trim().equals(userCols[j].trim())) {
							throw new InvalidParameterException(String.format("[%s] cannot appear more than once", userCols[i]));
						}
					}
				}
				// Unicity OK, moving on
				for (int i=0; i<userRows.length; i++) {
					String row = userRows[i];
					PinUtil.GPIOPin pin = PinUtil.findEnumName(row.trim());
					if (pin == null) {
						throw new InvalidParameterException(String.format("Unknown row pin name [%s]", row));
					} else {
						kpRow[i] = pin.pin();
					}
				}
				for (int i=0; i<userCols.length; i++) {
					String col = userCols[i];
					PinUtil.GPIOPin pin = PinUtil.findEnumName(col.trim());
					if (pin == null) {
						throw new InvalidParameterException(String.format("Unknown col pin name [%s]", col));
					} else {
						kpCol[i] = pin.pin();
					}
				}
			}
		}

		if (print) {
			System.out.println("     +-----+ +-----+ +-----+");
			System.out.println("     |  3  | |  2  | |  1  |   <- The keys you see");
			System.out.println("  +--+-----+-+-----+-+-----+--+");
			System.out.println("  |          T  O  P          |");
			System.out.println(" -+--+--+--+--+--+--+--+--+---+-");
			System.out.println("     |  |  |  |  |  |  |  |");
			System.out.println("     x  R  R  R  R  C  C  C");
			System.out.println("        1  2  3  4  1  2  3");
			System.out.println("        |  |  |  |  |  |  |");
			System.out.println("        |  |  |  |  |  |  Col [1,4,7,*] " + PinUtil.findByPin(kpCol[2]).toString() + ", " + PinUtil.findByPin(kpCol[2]).pinName());
			System.out.println("        |  |  |  |  |  Col [2,5,8,0] " + PinUtil.findByPin(kpCol[1]).toString() + ", " + PinUtil.findByPin(kpCol[1]).pinName());
			System.out.println("        |  |  |  |  Col [3,6,9,#] " + PinUtil.findByPin(kpCol[0]).toString() + ", " + PinUtil.findByPin(kpCol[0]).pinName());
			System.out.println("        |  |  |  Row [1,2,3] " + PinUtil.findByPin(kpRow[3]).toString() + ", " + PinUtil.findByPin(kpRow[3]).pinName());
			System.out.println("        |  |  Row [4,5,6] " + PinUtil.findByPin(kpRow[2]).toString() + ", " + PinUtil.findByPin(kpRow[2]).pinName());
			System.out.println("        |  Row [7,8,9] " + PinUtil.findByPin(kpRow[1]).toString() + ", " + PinUtil.findByPin(kpRow[1]).pinName());
			System.out.println("        Row [*,0,#] " + PinUtil.findByPin(kpRow[0]).toString() + ", " + PinUtil.findByPin(kpRow[0]).pinName());
			System.out.println();

			String[] map = new String[]{
					String.valueOf(PinUtil.findByPin(kpRow[0]).pinNumber()) + ":" + "R1",
					String.valueOf(PinUtil.findByPin(kpRow[1]).pinNumber()) + ":" + "R2",
					String.valueOf(PinUtil.findByPin(kpRow[2]).pinNumber()) + ":" + "R3",
					String.valueOf(PinUtil.findByPin(kpRow[3]).pinNumber()) + ":" + "R4",
					String.valueOf(PinUtil.findByPin(kpCol[0]).pinNumber()) + ":" + "C1",
					String.valueOf(PinUtil.findByPin(kpCol[1]).pinNumber()) + ":" + "C2",
					String.valueOf(PinUtil.findByPin(kpCol[2]).pinNumber()) + ":" + "C3"
			};

			PinUtil.print(map);
		}

		try {
			this.gpio = GpioFactory.getInstance();
		} catch (UnsatisfiedLinkError ule) {
			System.err.println("Not an a Pi, hey? You must be testing...");
		}
		rowButton = new GpioPinDigitalMultipurpose[kpRow.length];
		for (int i = 0; i < kpRow.length; i++) {
			if (this.verbose) {
				System.out.println(String.format("Provisioning %s", kpRow[i].toString()));
			}
			if (this.gpio != null) {
				try {
					rowButton[i] = this.gpio.provisionDigitalMultipurposePin(kpRow[i], PinMode.DIGITAL_INPUT);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} // else testing...
		}
		colButton = new GpioPinDigitalMultipurpose[kpCol.length];
		for (int i = 0; i < kpCol.length; i++) {
			if (this.verbose) {
				System.out.println(String.format("Provisioning %s", kpCol[i].toString()));
			}
			if (this.gpio != null) {
				try {
					colButton[i] = this.gpio.provisionDigitalMultipurposePin(kpCol[i], PinMode.DIGITAL_INPUT);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} // else testing
		}
		if (this.verbose) {
			System.out.println("All pins provisioned, keypad ready.");
		}
	}

	public char getKey() {
		char c = ' ';
		while (this.gpio != null && c == ' ') { // this.gpio != null => not testing
			// Set all column to output low
			for (int i = 0; i < colButton.length; i++) {
				colButton[i].setMode(PinMode.DIGITAL_OUTPUT);
				colButton[i].low();                            // set to LOW, very important
			}

			// All rows as input
			for (int i = 0; i < rowButton.length; i++) {
				rowButton[i].setMode(PinMode.DIGITAL_INPUT);
				rowButton[i].setPullResistance(PinPullResistance.PULL_UP); // pull UP
			}
			int row = -1, col = -1;
			// Scan rows for pushed keys
			for (int i = 0; i < rowButton.length; i++) {
				if (this.gpio.isLow(rowButton[i])) {  // Look for the row that is down (low), if any
					row = i;                            // This row was pushed because it is now LOW
					break;
				}
			}
			if (row != -1) {  // If a row has been touched...
				// Set (convert) columns to input
				for (int i = 0; i < colButton.length; i++) {
					colButton[i].setMode(PinMode.DIGITAL_INPUT);
					colButton[i].setPullResistance(PinPullResistance.PULL_DOWN);
				}
				// Scan cols for pushed keys
				for (int i = 0; i < kpCol.length; i++) { // If a row has been touched, then, if which column?
					if (this.gpio.isHigh(colButton[i])) {
						col = i;                          // This col was pushed because it is now HIGH
						break;
					}
				}
				if (col != -1) {
					c = keypad[row][col]; // The CHARACTER in the matrix
					reset();
		//    System.out.println(" >>> getKey: [" + row + ", " + col + "] = " + c);
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
		if (this.gpio != null) {
			this.gpio.setShutdownOptions(true);
			this.gpio.shutdown();
		}
	}
}
