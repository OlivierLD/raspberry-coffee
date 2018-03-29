package membrane;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalMultipurpose;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import utils.PinUtil;

import java.security.InvalidParameterException;

/**
 * Good explanation here: https://www.youtube.com/watch?v=yYnX5QodqQ4
 */
public class MembraneKeyPad1x4 {
	private GpioController gpio = null;
	private GpioPinDigitalMultipurpose commonLead = null;
	private GpioPinDigitalMultipurpose[] colButton = null;

	private final static char[] keypad = new char[]{'2', '1', '4', '3'};

	/*
	 * https://www.adafruit.com/products/1332
   */
	private Pin[] kpCol = new Pin[]{
			PinUtil.GPIOPin.GPIO_1.pin(),
			PinUtil.GPIOPin.GPIO_4.pin(),
			PinUtil.GPIOPin.GPIO_5.pin(),
			PinUtil.GPIOPin.GPIO_6.pin()
	};
	private Pin common = PinUtil.GPIOPin.GPIO_7.pin();

	public MembraneKeyPad1x4() {
		this(false);
	}

	public MembraneKeyPad1x4(boolean print) {

		// Default -Dkeypad.cols=GPIO_1,GPIO_4,GPIO_5,GPIO_6
		// Default -Dcommon.lead=GPIO_7
		String userProvidedCols = System.getProperty("keypad.cols");
		String userProvidedCommon = System.getProperty("common.lead");
		if (userProvidedCols != null || userProvidedCommon != null) {
			if (userProvidedCols == null || userProvidedCommon == null) {
				throw new InvalidParameterException("Please provide both keypad.cols AND common.lead, or none");
			}
			String[] userCols = userProvidedCols.split(",");
			if (userCols.length != 4) {
				throw new InvalidParameterException("keypad.cols should contain 4 elements, comma-separated.");
			}
			// Check unicity
			for (int i = 0; i < userCols.length; i++) {
				for (int j = i + 1; j < userCols.length; j++) {
					if (userCols[i].trim().equals(userCols[j].trim())) {
						throw new InvalidParameterException(String.format("[%s] cannot appear more than once", userCols[i]));
					}
				}
				if (userCols[i].trim().equals(userProvidedCommon.trim())) {
					throw new InvalidParameterException(String.format("[%s] cannot appear more than once", userCols[i]));
				}
			}
			// Unicity OK, moving on
			for (int i = 0; i < userCols.length; i++) {
				String col = userCols[i];
				PinUtil.GPIOPin pin = PinUtil.findEnumName(col.trim());
				if (pin == null) {
					throw new InvalidParameterException(String.format("Unknown row pin name [%s]", col));
				} else {
					kpCol[i] = pin.pin();
				}
			}

			PinUtil.GPIOPin pin = PinUtil.findEnumName(userProvidedCommon.trim());
			common = pin.pin();
		}

		if (print) {
			System.out.println("       Common, " + PinUtil.findByPin(common).toString() + ", " + PinUtil.findByPin(common).pinName());
			System.out.println("       |  " + PinUtil.findByPin(kpCol[0]).toString() + ", " + PinUtil.findByPin(kpCol[0]).pinName());
			System.out.println("       |  |  " + PinUtil.findByPin(kpCol[1]).toString() + ", " + PinUtil.findByPin(kpCol[1]).pinName());
			System.out.println("       |  |  |  " + PinUtil.findByPin(kpCol[2]).toString() + ", " + PinUtil.findByPin(kpCol[2]).pinName());
			System.out.println("       |  |  |  |  " + PinUtil.findByPin(kpCol[3]).toString() + ", " + PinUtil.findByPin(kpCol[3]).pinName());
			System.out.println("       |  |  |  |  | ");
			System.out.println("       x  C  C  C  C ");
			System.out.println("       |  1  2  3  4 ");
			System.out.println("       |  |  |  |  | ");
			System.out.println("       |  |  |  |  | ");
			System.out.println("       |  |  |  |  | ");
			System.out.println(" +----------------------+");
			System.out.println(" | +---++---++---++---+ |");
			System.out.println(" | | 1 || 2 || 3 || 4 | |");
			System.out.println(" | +---++---++---++---+ |");
			System.out.println(" +----------------------+");
			System.out.println();
			PinUtil.print();
		}

		try {
			this.gpio = GpioFactory.getInstance();
		} catch (UnsatisfiedLinkError ule) {
			System.err.println("Not an a Pi, hey? You must be testing...");
		}
		if (this.gpio != null) {
			commonLead = this.gpio.provisionDigitalMultipurposePin(common, PinMode.DIGITAL_INPUT);
			colButton = new GpioPinDigitalMultipurpose[kpCol.length];
			for (int i = 0; i < kpCol.length; i++) {
				if (this.gpio != null) {
					colButton[i] = this.gpio.provisionDigitalMultipurposePin(kpCol[i], PinMode.DIGITAL_INPUT);
				} // else testing
			}
		}
		if ("true".equals(System.getProperty("keypad.verbose", "false"))) {
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
			commonLead.setPullResistance(PinPullResistance.PULL_UP); // pull UP

			int col = -1;
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
				c = keypad[col]; // The CHARACTER in the matrix
				reset();
				//    System.out.println(" >>> getKey: [" + row + ", " + col + "] = " + c);
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
		if (commonLead != null) {
			commonLead.setMode(PinMode.DIGITAL_INPUT);
			commonLead.setPullResistance(PinPullResistance.PULL_UP);
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
			this.gpio.shutdown();
		}
	}
}
