package sevensegdisplay.samples;

import com.pi4j.io.i2c.I2CFactory;
import sevensegdisplay.SevenSegment;

import java.io.IOException;
import java.util.Set;

public class AllCharSample {
	private static SevenSegment segment = null;

	public static void main(String... args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		segment = new SevenSegment(0x70, true);

		String[] displayed = {" ", " ", " ", " "};

		Set<String> allChars = SevenSegment.ALL_CHARS.keySet();
		for (String c : allChars) {
			System.out.println("--> " + c);
			displayed = scrollLeft(displayed, c);
			fullDisplay(displayed);
			try {
				Thread.sleep(500L);
			} catch (InterruptedException ie) {
			}
		}
		try {
			Thread.sleep(3_000L);
		} catch (InterruptedException ie) {
		}

		for (int i = 0; i < 4; i++) {
			fullDisplay(new String[]{"C", "A", "F", "E"});
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
			}
			fullDisplay(new String[]{"B", "A", "B", "E"});
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
			}
		}
		segment.clear();
	}

	private static String[] scrollLeft(String[] row, String c) {
		String[] newSa = row.clone();
		for (int i = 0; i < row.length - 1; i++) {
			newSa[i] = row[i + 1];
		}
		newSa[row.length - 1] = c;
		return newSa;
	}

	private static void fullDisplay(String[] row) throws IOException {
		segment.writeDigitRaw(0, row[0]);
		segment.writeDigitRaw(1, row[1]);
		segment.writeDigitRaw(3, row[2]);
		segment.writeDigitRaw(4, row[3]);
	}
}
