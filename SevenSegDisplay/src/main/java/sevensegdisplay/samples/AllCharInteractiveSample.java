package sevensegdisplay.samples;

import com.pi4j.io.i2c.I2CFactory;
import sevensegdisplay.SevenSegment;

import java.io.IOException;

import static utils.StaticUtil.userInput;

public class AllCharInteractiveSample {

	private static SevenSegment segment = null;

	public static void main(String... args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		segment = new SevenSegment(0x70, true);

		boolean go = true;
		System.out.println("Enter 'quit' to quit...");
		while (go) {
			String input = userInput("Enter a string (up to 4 char) > ");
			if ("quit".equalsIgnoreCase(input)) {
				go = false;
			} else {
				String[] row = {" ", " ", " ", " "};
				for (int i = 0; i < Math.min(input.length(), 4); i++) {
					String one = input.substring(i, i + 1);
					Byte b = SevenSegment.ALL_CHARS.get(one);
					if (b != null) {
						row[i] = one;
					} else {
						System.out.println(one + " not in the list.");
						row[i] = " ";
					}
				}
				fullDisplay(row);
			}
		}
		System.out.println("Bye");
		segment.clear();
	}

	private static void fullDisplay(String[] row) throws IOException {
		segment.writeDigitRaw(0, row[0]);
		segment.writeDigitRaw(1, row[1]);
		segment.writeDigitRaw(3, row[2]);
		segment.writeDigitRaw(4, row[3]);
	}
}
