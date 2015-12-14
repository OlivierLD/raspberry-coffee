package raspisamples.wp;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

/*
 * PWM with WiringPi
 */
public class WiringPiSoftPWMExample
{
  public static void main(String[] args)
    throws InterruptedException
  {
    // initialize wiringPi library
    com.pi4j.wiringpi.Gpio.wiringPiSetup();
    int pinAddress = RaspiPin.GPIO_01.getAddress(); 
    // create soft-pwm pins (min=0 ; max=100)
//  SoftPwm.softPwmCreate(1, 0, 100); 
    SoftPwm.softPwmCreate(pinAddress, 0, 100); 

    // continuous loop
    boolean go = true;
    for (int idx=0; idx<5; idx++)
    {
      // fade LED to fully ON
      for (int i = 0; i <= 100; i++)
      {
        SoftPwm.softPwmWrite(1, i);
        Thread.sleep(10);
      }

      // fade LED to fully OFF
      for (int i = 100; i >= 0; i--)
      {
        SoftPwm.softPwmWrite(1, i);
        Thread.sleep(10);
      }
    }
  }
}
