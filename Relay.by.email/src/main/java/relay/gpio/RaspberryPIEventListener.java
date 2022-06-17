package relay.gpio;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;

public interface RaspberryPIEventListener {
    void manageEvent(GpioPinDigitalStateChangeEvent event);
}
