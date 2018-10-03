package samples;

import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.system.SystemInfo;
import i2c.sensor.BMP180;
import sevensegdisplay.SevenSegment;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * Two devices on the I2C bus.
 * A BMP180, and a 7-segment backpack display HT16K33
 * mounted serially (V3V, GND, SDA, SLC)
 */
public class SevenSegBMP180 {
	private static boolean go = true;
	private static long wait = 2_000L;

	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		final NumberFormat NF = new DecimalFormat("##00.00");
		BMP180 sensor = new BMP180();
		final SevenSegment segment = new SevenSegment(0x70, true);

		Runtime.getRuntime().addShutdownHook(new Thread("Hook") {
			public void run() {
				System.out.println("\nQuitting");
				try {
					segment.clear();
				} catch (Exception ex) {
				}
				System.out.println("Bye-bye");
				go = false;
			}
		});


		while (go) {
			float temp = 0;

			try {
				temp = sensor.readTemperature();
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}

			System.out.println("Temperature: " + NF.format(temp) + " C");
			try {
				displayString("TEMP", segment);
				try {
					Thread.sleep(wait);
				} catch (InterruptedException ie) {
				}
				displayFloat(temp, segment);
				try {
					Thread.sleep(wait);
				} catch (InterruptedException ie) {
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			// Bonus : CPU Temperature
			try {
				float cpu = SystemInfo.getCpuTemperature();
				System.out.println("CPU Temperature   :  " + cpu);
				System.out.println("CPU Core Voltage  :  " + SystemInfo.getCpuVoltage());
				displayString("CPU ", segment);
				try {
					Thread.sleep(wait);
				} catch (InterruptedException ie) {
				}
				displayFloat(cpu, segment);
				try {
					Thread.sleep(wait);
				} catch (InterruptedException ie) {
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void displayFloat(float t, SevenSegment segment) throws IOException {
		int one = (int) t / 10;
		int two = ((int) t) % 10;
		int three = ((int) (10 * t) % 10);
		int four = ((int) (100 * t) % 10);

//  System.out.println(one + " " + two + "." + three + " " + four);
		segment.writeDigit(0, one);
		segment.writeDigit(1, two, true);
		segment.writeDigit(3, three);
		segment.writeDigit(4, four);
	}

	private static void displayString(String row, SevenSegment segment) throws IOException {
		segment.writeDigitRaw(0, row.substring(0, 1));
		segment.writeDigitRaw(1, row.substring(1, 2));
		segment.writeDigitRaw(3, row.substring(2, 3));
		segment.writeDigitRaw(4, row.substring(3, 4));
	}
}
