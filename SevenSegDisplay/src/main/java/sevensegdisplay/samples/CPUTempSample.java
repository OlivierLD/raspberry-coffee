package sevensegdisplay.samples;

import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.system.SystemInfo;
import sevensegdisplay.SevenSegment;

import java.io.IOException;

public class CPUTempSample {
	private static boolean go = true;

	private static void setGo(boolean b) {
		go = b;
	}

	public static void main(String... args) throws IOException, InterruptedException, I2CFactory.UnsupportedBusNumberException {
		SevenSegment segment = new SevenSegment(0x70, true);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				setGo(false);
			}
		});

		while (go) {
			float cpuTemp = SystemInfo.getCpuTemperature();
			// Notice the digit index: 0, 1, 3, 4. 2 is the column ":"
			int one = (int) cpuTemp / 10;
			int two = ((int) cpuTemp) % 10;
			int three = ((int) (10 * cpuTemp) % 10);
			int four = ((int) (100 * cpuTemp) % 10);

//    System.out.println(one + " " + two + "." + three + " " + four);
			segment.writeDigit(0, one);
			segment.writeDigit(1, two, true);
			segment.writeDigit(3, three);
			segment.writeDigit(4, four);
//    System.out.println("Temp:" + cpuTemp);
			try {
				Thread.sleep(1_000L);
			} catch (InterruptedException ie) {
			}
		}
		segment.clear();
	}
}
