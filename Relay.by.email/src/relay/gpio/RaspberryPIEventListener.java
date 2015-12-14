package relay.gpio;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;

public interface RaspberryPIEventListener
{
  public void manageEvent(GpioPinDigitalStateChangeEvent event);
}
