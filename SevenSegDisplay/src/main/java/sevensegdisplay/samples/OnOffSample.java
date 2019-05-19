package sevensegdisplay.samples;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory;
import sevensegdisplay.SevenSegment;

public class OnOffSample {
	public static void main(String... args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		SevenSegment segment = new SevenSegment(0x70, true);

		for (int i = 0; i < 5; i++) {
			// Notice the digit index: 0, 1, 3, 4. Index 2 is the column ":"
			segment.writeDigit(0, 8, true);
			segment.writeDigit(1, 8, true);
			segment.writeDigit(3, 8, true);
			segment.writeDigit(4, 8, true);
			segment.setColon();
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
			}
			segment.clear();
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
			}
		}
	}
}
