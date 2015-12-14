package relay.gpio;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class GPIOController
{
  private GpioController gpio = null;
  private OneRelay relay = null;
  
  public GPIOController()
  {
    this.gpio = GpioFactory.getInstance();
    this.relay = new OneRelay(this.gpio, RaspiPin.GPIO_00, "Relay01");
  }
    
  public void shutdown()
  {
    this.gpio.shutdown();
  }
  
  public void switchRelay(boolean on)
  {
    if (on)
      relay.on();
    else
      relay.off();
  }  
}
