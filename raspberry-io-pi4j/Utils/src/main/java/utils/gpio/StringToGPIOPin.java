package utils.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class StringToGPIOPin {

    public static Pin stringToGPIOPin(String pinName) {
        Pin pin =  RaspiPin.getPinByName(pinName);
        return pin;
    }
}
