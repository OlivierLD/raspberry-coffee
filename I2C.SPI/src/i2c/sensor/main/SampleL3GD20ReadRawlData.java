package i2c.sensor.main;

import i2c.sensor.L3GD20;
import i2c.sensor.utils.L3GD20Dictionaries;

/*
 * Read real data
 */
public class SampleL3GD20ReadRawlData
{
  private boolean go = true;
  
  public SampleL3GD20ReadRawlData() throws Exception
  {
    L3GD20 sensor = new L3GD20();
    sensor.setPowerMode(L3GD20Dictionaries.NORMAL);
    sensor.setAxisXEnabled(false);
    sensor.setAxisYEnabled(false);
    sensor.setAxisZEnabled(true);
    sensor.setDataRateAndBandwidth(95, 12.5f);
    sensor.setFifoModeValue(L3GD20Dictionaries.BYPASS);
    
    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
                                           public void run()
                                           {
                                             go = false;
                                             System.out.println("\nBye.");
                                           }
                                         });    
//  sensor.init();
    sensor.calibrateZ();

    while (go) // TODO Put a Tmax
    {      
      while (sensor.getAxisDataAvailableValue()[2] == 0)
        try { Thread.sleep(1L); } catch (InterruptedException ex) {}
      
      double z = sensor.getCalOutZValue();
      System.out.printf("Z:%.2f%n", z);
    }
  }
  
  public static void main(String[] args) throws Exception
  {
    SampleL3GD20ReadRawlData main = new SampleL3GD20ReadRawlData();
  }
}
