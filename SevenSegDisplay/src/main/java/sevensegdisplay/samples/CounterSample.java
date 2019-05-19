package sevensegdisplay.samples;

import com.pi4j.io.i2c.I2CFactory;
import sevensegdisplay.SevenSegment;

import java.io.IOException;

public class CounterSample {
	public static void main(String... args) throws IOException, I2CFactory.UnsupportedBusNumberException {
		SevenSegment segment = new SevenSegment(0x70, true);

		long before = System.currentTimeMillis();
		for (int i = 0; i < 10_000; i++) {
			// Notice the digit index: 0, 1, 3, 4. 2 is the column ":"
			segment.writeDigit(0, (i / 1_000));     // 1000th
			segment.writeDigit(1, (i / 100) % 10); // 100th
			segment.writeDigit(3, (i / 10) % 10);  // 10th
			segment.writeDigit(4, i % 10);         // Ones
			try {
				Thread.sleep(10L);
			} catch (InterruptedException ie) {
			}
		}
		long after = System.currentTimeMillis();
		System.out.println("Took " + Long.toString(after - before) + " ms.");
		try {
			Thread.sleep(1_000L);
		} catch (InterruptedException ie) {
		}
		segment.clear();
	}
}
