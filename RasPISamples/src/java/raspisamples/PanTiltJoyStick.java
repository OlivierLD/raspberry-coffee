package raspisamples;

import com.pi4j.io.i2c.I2CFactory;
import raspisamples.adc.JoyStick;

import raspisamples.adc.JoyStickClient;

import raspisamples.servo.StandardServo;

/*
 * Joystick read with ADC (MCP3008)
 * 2 Servos (UP/LR)
 */
public class PanTiltJoyStick
{
  private static StandardServo ssUD = null, 
                               ssLR = null;
  private static JoyStick joyStick = null;
  
  public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException
  {
    ssUD = new StandardServo(14); // 14 : Address on the board (1..15)
    ssLR = new StandardServo(15); // 15 : Address on the board (1..15)
    
    // Init/Reset
    ssUD.stop();
    ssLR.stop();
    ssUD.setAngle(0f);
    ssLR.setAngle(0f);
    
    StandardServo.waitfor(2000);

    JoyStickClient jsc = new JoyStickClient()
    {
      @Override
      public void setUD(int v) // 0..100
      {
        float angle = (float)(v - 50) * (9f / 5f); // conversion from 1..100 to -90..+90
        if ("true".equals(System.getProperty("verbose", "false")))
          System.out.println("UD:" + v + ", -> " + angle + " deg.");
        ssUD.setAngle(angle); // -90..+90
      }

      @Override
      public void setLR(int v) // 0..100
      {
        float angle = (float)(v - 50) * (9f / 5f); // conversion from 1..100 to -90..+90
        if ("true".equals(System.getProperty("verbose", "false")))
          System.out.println("LR:" + v + ", -> " + angle + " deg.");
        ssLR.setAngle(angle); // -90..+90
      }
    };

    Runtime.getRuntime().addShutdownHook(new Thread()
       {
         public void run()
         {
           ssUD.setAngle(0f);
           ssLR.setAngle(0f);
           StandardServo.waitfor(500);
           ssUD.stop();
           ssLR.stop();
           System.out.println("\nBye (Ctrl+C)");      
         }
       });    
    
    try
    {
      joyStick = new JoyStick(jsc);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      ssUD.setAngle(0f);
      ssLR.setAngle(0f);
      StandardServo.waitfor(500);
      ssUD.stop();
      ssLR.stop();
      System.out.println("Bye");      
    }
  }
}
