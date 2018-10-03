package samples;

import i2c.sensor.VCNL4000;
import sevensegdisplay.SevenSegment;

import java.io.IOException;

public class VCNL4000ProximityWithDisplay {
	private static boolean go = true;
	private final static int MIN_AMBIENT = 0;
	private final static int MAX_AMBIENT = 5500;

	private static VCNL4000 sensor;
	private static SevenSegment display;

	public static void main(String... args) throws Exception {
		sensor = new VCNL4000();
		display = new SevenSegment(0x70, true);

		int prox = 0;
		int ambient = 0;

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
			try {
				display.clear();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			System.out.println("\nBye");
		}));
		while (go) { //  && i++ < 5)
			try {
				//      prox = sensor.readProximity();
				int[] data = sensor.readAmbientProximity();
				prox = data[VCNL4000.PROXIMITY_INDEX];
				ambient = data[VCNL4000.AMBIENT_INDEX];
			} catch (Exception ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
			}
//    System.out.println("Ambient:" + ambient + ", Proximity: " + prox); //  + " unit?");
			int amb = /* 100 - */ Math.min((int) Math.round(100f * ((float) ambient / (float) (MAX_AMBIENT - MIN_AMBIENT))), 100);
			System.out.println("Ambient:" + ambient + ", Proximity: " + prox + ", " + amb);
			// Notice the digit index: 0, 1, 3, 4. 2 is the column ":"
			int one = amb / 1_000;
			int two = (amb - (one * 1_000)) / 100;
			int three = (amb - (one * 1_000) - (two * 100)) / 10;
			int four = amb % 10;

//    System.out.println("  --> " + proxPercent + " : " + one + " " + two + "." + three + " " + four);

			if (one > 0) {
				display.writeDigit(0, one);
			} else {
				display.writeDigitRaw(0, " ");
			}
			if (two > 0 || one > 0) {
				display.writeDigit(1, two);
			} else {
				display.writeDigitRaw(1, " ");
			}
			if (one > 0 || two > 0 || three > 0) {
				display.writeDigit(3, three);
			} else {
				display.writeDigitRaw(3, " ");
			}
			display.writeDigit(4, four);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException ex) {
				System.err.println(ex.toString());
			}
		}
	}
}
