package dac.mcp4725;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public class MCP4725
{
  public final static int MCP4725_ADDRESS = 0x62; // Can be changed with pin A0. A0 connected to VDD: 0x63

  public final static int MCP4725_REG_WRITEDAC       = 0x40;
  public final static int MCP4725_REG_WRITEDACEEPROM = 0x60;

  private static boolean verbose = true;

  private I2CBus bus;
  private I2CDevice mcp4725;

  public MCP4725() throws I2CFactory.UnsupportedBusNumberException
  {
    this(MCP4725_ADDRESS);
  }

  public MCP4725(int address) throws I2CFactory.UnsupportedBusNumberException
  {
    try
    {
      // Get i2c bus
      bus = I2CFactory.getInstance(I2CBus.BUS_1); // Depends on the RasPi version
      if (verbose)
        System.out.println("Connected to bus. OK.");

      // Get device itself
      mcp4725 = bus.getDevice(address);
      if (verbose)
        System.out.println("Connected to device. OK.");
    }
    catch (IOException e)
    {
      System.err.println(e.getMessage());
    }
  }

  // Set the voltage, readable on the VOUT terminal
  public void setVoltage(int voltage) // throws IOException
  {
    try { setVoltage(voltage, false); }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
    catch (NullPointerException npe)
    {
      npe.printStackTrace();
    }
  }

  public void setVoltage(int voltage, boolean persist) throws IOException, NullPointerException
  {
    voltage = Math.min(voltage, 4095);  // 4096 = 2^12
    voltage = Math.max(voltage, 0);
    byte[] bytes = new byte[2];
    bytes[0] = (byte)((voltage >> 4) & 0xff);
    bytes[1] = (byte)((voltage << 4) & 0xff);
    if (persist)
      this.mcp4725.write(MCP4725_REG_WRITEDACEEPROM, bytes, 0, 2);
    else
      this.mcp4725.write(MCP4725_REG_WRITEDAC, bytes, 0, 2);
  }
}
