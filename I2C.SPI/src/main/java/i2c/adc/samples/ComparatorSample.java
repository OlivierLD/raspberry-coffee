package i2c.adc.samples;

import com.pi4j.io.i2c.I2CFactory;
import i2c.adc.ADS1x15;

public class ComparatorSample
{
  private static boolean go = true;

  private final static void setGo(boolean b)
  {
    go = b;
  }

  public static void main(String... args) throws I2CFactory.UnsupportedBusNumberException
  {
    final ADS1x15 adc = new ADS1x15(ADS1x15.ICType.IC_ADS1115);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Stop reading.");
      adc.stopContinuousConversion();
      try { Thread.sleep(250L); } catch (Exception ex) {}
      setGo(false);
    }));
    adc.startSingleEndedComparator(ADS1x15.Channels.CHANNEL_2, 200, 100, 1024, 250);
    while (go)
    {
      System.out.println("Channel 2:" + adc.getLastConversionResults() / 1_000.0);
      try { Thread.sleep(250L); } catch (Exception ex) {}
    }
  }
}
