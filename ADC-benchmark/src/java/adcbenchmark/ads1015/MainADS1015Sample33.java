package adcbenchmark.ads1015;

import com.pi4j.io.i2c.I2CFactory;
import i2c.adc.ADS1x15;

public class MainADS1015Sample33 {
	private static ADS1x15 ads1015 = null;
	private final static ADS1x15.ICType ADC_TYPE = ADS1x15.ICType.IC_ADS1015;
	private static int gain = 6_144;
	private static int sps  = 250;

	private static boolean go = true;

	public static void main(String... args) {
		try {
			ads1015 = new ADS1x15(ADC_TYPE);
		} catch (I2CFactory.UnsupportedBusNumberException usbne) {
			throw new RuntimeException(usbne);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
		}));

		while (go) {
			float voltage;
			float value = ads1015.readADCSingleEnded(
					ADS1x15.Channels.CHANNEL_0,
					gain,
					sps);
			System.out.println("Voltage Value:" + value);
			voltage = value / 1_000f;
		}
		System.out.println("Bye!");
	}
}
