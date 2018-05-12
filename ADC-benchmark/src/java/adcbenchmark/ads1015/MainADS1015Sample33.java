package adcbenchmark.ads1015;

import com.pi4j.io.i2c.I2CFactory;
import i2c.adc.ADS1x15;

public class MainADS1015Sample33 {

	private final static int _2_POW_12 = 4095;

	private static ADS1x15 ads1015 = null;

	private static ADS1x15.pgaADS1x15 gainRef = ADS1x15.pgaADS1x15.ADS1015_REG_CONFIG_PGA_4_096V;

	private static int gain = gainRef.meaning(); // 6_144;
	private static int sps  = 250;

	private static boolean go = true;

	public static void main(String... args) {
		try {
			ads1015 = new ADS1x15(ADS1x15.ICType.IC_ADS1015);
		} catch (I2CFactory.UnsupportedBusNumberException usbne) {
			throw new RuntimeException(usbne);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			go = false;
		}));

		while (go) {
			float value = ads1015.readADCSingleEnded(
					ADS1x15.Channels.CHANNEL_0,
					gain,
					sps);
			System.out.println(String.format("ADC Value: %f, Voltage: %.02f ", value, gain * (value / _2_POW_12)));
		}
		System.out.println("Bye!");
	}
}
