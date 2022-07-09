package utils.gpio;

import org.junit.Test;
import com.pi4j.io.gpio.Pin;

import static org.junit.Assert.assertTrue;

public class GetPinByName {

    @Test
    public void testByName() {
        Pin gpio_08 = StringToGPIOPin.stringToGPIOPin("GPIO 8");
        assertTrue("Pin is null, or un-expected", gpio_08 != null && gpio_08.getAddress() == 8);
    }
}
