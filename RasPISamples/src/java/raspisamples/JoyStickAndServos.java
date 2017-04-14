package raspisamples;

import com.pi4j.io.i2c.I2CFactory;
import raspisamples.adc.JoyStick;

import raspisamples.adc.JoyStickClient;

import raspisamples.servo.StandardServo;

/*
 * Joystick read with ADC (MCP3008)
 * 2 Servos (UP/LR)
 */
public class JoyStickAndServos
{
  private static StandardServo ss1 = null, ss2 = null;
  private static JoyStick joyStick = null;
  
  public static void main(String[] args) throws I2CFactory.UnsupportedBusNumberException
  {
    ss1 = new StandardServo(13); // 13 : Address on the board (1..15)
    ss2 = new StandardServo(15); // 15 : Address on the board (1..15)
    
    ss1.stop();
    ss2.stop();
    JoyStickClient jsc = new JoyStickClient()
    {
      @Override
      public void setUD(int v) // 0..100
      {
        float angle = (float)(v - 50) * (9f / 5f);
        ss1.setAngle(angle); // -90..+90
      }

      @Override
      public void setLR(int v) // 0..100
      {
        float angle = (float)(v - 50) * (9f / 5f);
        ss2.setAngle(angle); // -90..+90
      }
    };

    Runtime.getRuntime().addShutdownHook(new Thread()
       {
         public void run()
         {
           ss1.stop();
           ss2.stop();
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
      ss1.stop();
      ss2.stop();
      System.out.println("Bye");      
    }
  }
}
