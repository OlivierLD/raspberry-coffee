package adcbenchmark.ads1015;

import com.pi4j.io.i2c.I2CFactory;
import i2c.adc.ADS1x15;

public class MainADS1015Sample33 {

	private static ADS1x15 ads1015 = null; // Device

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
			System.out.println("\nOut of here.");
			go = false;
		}));

		int prevValue = -1;
		while (go) {
			int value = (int)ads1015.readADCSingleEnded(
					ADS1x15.Channels.CHANNEL_0,
					gain,
					sps);
			if (prevValue != value) {
				System.out.println(String.format("ADC Value: %d, Voltage: %.05f ", value, (value / 1_000f))); // 1_000f: 4_096 to 4.096
			}
			prevValue = value;
		}
		System.out.println("Bye!");
	}
}
