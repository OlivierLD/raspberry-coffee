package i2c.sensor.main;

import i2c.sensor.L3GD20;
import i2c.sensor.utils.L3GD20Dictionaries;

/*
 * Read real data
 */
public class SampleL3GD20ReadRealData {
	private boolean go = true;

	public SampleL3GD20ReadRealData() throws Exception {
		L3GD20 sensor = new L3GD20();
		sensor.setPowerMode(L3GD20Dictionaries.NORMAL);
		sensor.setFullScaleValue(L3GD20Dictionaries._250_DPS);
		sensor.setAxisXEnabled(true);
		sensor.setAxisYEnabled(true);
		sensor.setAxisZEnabled(true);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				go = false;
				System.out.println("\nBye.");
			}, "Shutdown Hook"));
		sensor.init();
		sensor.calibrate();
		long wait = 20L;
		double x = 0, y = 0, z = 0;
		while (go) {
			double[] data = sensor.getCalOutValue();
			x = data[0];
			y = data[1];
			z = data[2];
//    x += (data[0] * wait);
//    y += (data[1] * wait);
//    z += (data[2] * wait);
			System.out.printf("X:%.2f, Y:%.2f, Z:%.2f%n", x, y, z);
			try {
				Thread.sleep(wait);
			} catch (InterruptedException ex) {
			}
		}
	}

	public static void main(String... args) throws Exception {
		new SampleL3GD20ReadRealData();
	}
}
