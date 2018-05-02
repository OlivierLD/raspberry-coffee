package i2c.adc.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.adc.ADS1x15;

public class SingleEndedSample {
	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		final ADS1x15 adc = new ADS1x15(ADS1x15.ICType.IC_ADS1115);
		int gain = 4_096;
		int sps = 250;
		float value = adc.readADCSingleEnded(ADS1x15.Channels.CHANNEL_2, gain, sps);
		System.out.printf("%.6f\n", (value / 1_000f));
	}
}
