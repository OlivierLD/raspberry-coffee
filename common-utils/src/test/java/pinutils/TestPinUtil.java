package pinutils;

import org.junit.Test;
import utils.GenericPinUtil;

import static org.junit.Assert.assertEquals;

public class TestPinUtil {
    public static void main(String... args) {
        GenericPinUtil.print();

        // List pins
        for (GenericPinUtil.GPIOPin gpioPin : GenericPinUtil.GPIOPin.values()) {
            System.out.printf("%s is named %s\n", gpioPin.toString(), gpioPin.pinName());
        }

        System.out.println("\nAs for an MCP3008 (Physical:Label):");
        GenericPinUtil.print("23:CLK", "21:Dout", "19:Din", "24:CS");

        System.out.println("\nAs for an MCP3008, with GPIO prefix (Physical:Label):");
        GenericPinUtil.print(true, "23:CLK", "21:Dout", "19:Din", "24:CS");

        int physical = GenericPinUtil.getPhysicalByWiringPiNumber("GPIO 29");
        System.out.printf("Physical by WiringPi number: GPIO_29 => #%d\n", physical); // Should be #40

        String sdaLabel = String.valueOf(GenericPinUtil.findEnumName("SDA1").pinNumber()) + ":" + "SDA";
        System.out.println(">> SDA Label example : " + sdaLabel);
    }

    @Test
    public void testPin40() {
        int physical = GenericPinUtil.getPhysicalByWiringPiNumber("GPIO 29");
        assertEquals("Wiring RaspiPin.GPIO_29 should be physical 40", 40, physical); // Should be #40
    }

    @Test
    public void testSDALabel() {
        // 3:SDA
        String sdaLabel = String.valueOf(GenericPinUtil.findEnumName("SDA1").pinNumber()) + ":" + "SDA";
        assertEquals(">> SDA Label example : ", "3:SDA", sdaLabel);
    }
}
