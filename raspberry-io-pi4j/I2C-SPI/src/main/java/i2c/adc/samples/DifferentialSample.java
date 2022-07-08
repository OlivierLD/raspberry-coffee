package i2c.adc.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.adc.ADS1x15;

public class DifferentialSample {
	public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException {
		final ADS1x15 adc = new ADS1x15(ADS1x15.ICType.IC_ADS1115);
		int gain = 4_096;
		int sps = 250;

		float volt2 = adc.readADCSingleEnded(ADS1x15.Channels.CHANNEL_2, gain, sps) / 1_000;
		float volt3 = adc.readADCSingleEnded(ADS1x15.Channels.CHANNEL_3, gain, sps) / 1_000;

		float voltDiff = adc.readADCDifferential23(gain, sps) / 1_000;
		System.out.printf("%.8f %.8f %.8f %.8f \n", volt2, volt3, (volt3 - volt2), -voltDiff);
	}
}
