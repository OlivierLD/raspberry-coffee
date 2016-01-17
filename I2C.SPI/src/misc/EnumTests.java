package misc;

import i2c.adc.ADS1x15.spsADS1115;

public class EnumTests
{
  private static boolean verbose = true;
  
  public enum spsADS1015
  {
    ADS1015_REG_CONFIG_DR_128SPS(128),
    ADS1015_REG_CONFIG_DR_250SPS(250),
    ADS1015_REG_CONFIG_DR_490SPS(490),
    ADS1015_REG_CONFIG_DR_920SPS(920),
    ADS1015_REG_CONFIG_DR_1600SPS(1600),
    ADS1015_REG_CONFIG_DR_2400SPS(2400),
    ADS1015_REG_CONFIG_DR_3300SPS(3300);

    private final int value;
    spsADS1015(int value) { this.value = value; }    
    public int value() { return this.value; }
    
    public static int defaultVal(int val, int def)
    {
      int ret = def;
      boolean found = false;
      for (spsADS1015 one : values())
      {
        if (one.value() == val)
        {
          ret = val;
          found = true;
          break;
        }
      }
      if (!found)
      {
        if (verbose)
          System.out.println("Value [" + val + "] not found, defaulting to [" + def + "]");
        // Check if default value is in the list
        found = false;
        for (spsADS1015 one : values())
        {
          if (one.value() == def)
          {
            ret = val;
            found = true;
            break;
          }
        }
        if (!found)
        {
          System.out.println("Just FYI... default value is not in the enum...");
        }
      }
      return ret;
    }
  }

  public static int defaultSpsADS1015(int val, int defaultVal)
  {
    int ret = defaultVal;
    for (spsADS1015 v : spsADS1015.values())
    {
      if (v.value() == val)
      {
        ret = val;
        break;
      }
    }
    return ret;
  }
  
  public static void main(String[] args)
  {
    System.out.println("Value:" + defaultSpsADS1015(2400, 1234));
    System.out.println("Value:" + defaultSpsADS1015(7890, 1234));
    
    System.out.println("DefValue:" + spsADS1015.defaultVal(2400, 1234));
    System.out.println("DefValue:" + spsADS1015.defaultVal(6789, 1234));
    
    System.out.println("Done");
  }

}
