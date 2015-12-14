package relay;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class RelayManager
{
  private final GpioController gpio = GpioFactory.getInstance();
  private final GpioPinDigitalOutput pin17;
  private final GpioPinDigitalOutput pin18;
  
  public RelayManager()
  {
    System.out.println("GPIO Control - pin 00/#17 and 01/#18 ... started.");
    System.out.println("(Labelled #17 an #18 on the cobbler.)");

    // For a relay it seems that HIGH means NC (Normally Closed)...
    pin17 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Relay1", PinState.HIGH);
    pin18 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "Relay2", PinState.HIGH);
  }

  public void set(String device, String status)
  {
    GpioPinDigitalOutput pin = ("01".equals(device)?pin17:pin18);
    if ("on".equals(status))
      pin.low();
    else
      pin.high();
  }
  
  public void shutdown()
  {
    gpio.shutdown();
  }
}
