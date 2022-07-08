package sevensegdisplay.samples;

import com.pi4j.io.i2c.I2CFactory;
import sevensegdisplay.SevenSegment;

import java.io.IOException;

import static utils.StaticUtil.userInput;

public class InteractiveSample {

	public static void main(String... args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		SevenSegment segment = new SevenSegment(0x70, true);
		boolean go = true;
		System.out.println("Enter 'quit' to quit...");
		while (go) {
			String input = userInput("Number to display [0..F] > ");
			if ("quit".equalsIgnoreCase(input)) {
				go = false;
			} else {
				int digit = 0;
				boolean digitOk = true;
				try {
					digit = Integer.parseInt(input, 16);
					if (digit < 0 || digit > 0xF) {
						System.out.println("Invalid digit");
						digitOk = false;
					}
				} catch (NumberFormatException nfe) {
					System.out.println(nfe.toString());
					digitOk = false;
				}
				if (digitOk) {
					input = userInput("Position [0..7] > ");
					int pos = 0;
					boolean posOk = true;
					try {
						pos = Integer.parseInt(input);
						if (pos < 0 || pos > 7) {
							posOk = false;
							System.out.println("Invalid position");
						}
					} catch (NumberFormatException nfe) {
						System.out.println(nfe.toString());
						posOk = false;
					}
					if (digitOk && posOk) {
						segment.writeDigit(pos, digit); // Display
					}
				}
			}
		}
		System.out.println("Bye");
		segment.clear();
	}
}
