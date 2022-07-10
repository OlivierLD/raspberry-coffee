package utils;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility, to display the Raspberry Pi's header 40 pins
 * in their (many) different lingos...
 */
public class PinUtil {
    // Disposed as on the board
    public enum GPIOPin {
        // IO: BCM, JOB, gpio, onoff.js
        // WiPi: WiringPI, Pi4J
        //
        //      Name            #  IO WiPi                            Name            #  IO WiPi
        PWR_1("3v3",            1, -1, -1, null),             PWR_2("5v0",            2, -1, -1, null),
        GPIO_8("SDA1",          3,  2,  8, RaspiPin.GPIO_08), PWR_3("5v0",            4, -1, -1, null),
        GPIO_9("SCL1",          5,  3,  9, RaspiPin.GPIO_09), GRND_1("GND",           6, -1, -1, null),
        GPIO_7("GPCLK0",        7,  4,  7, RaspiPin.GPIO_07), GPIO_15("UART0_TXD",    8, 14, 15, RaspiPin.GPIO_15),
        GRND_2("GND",           9, -1, -1, null),             GPIO_16("UART0_RXD",   10, 15, 16, RaspiPin.GPIO_16),
        GPIO_0("GPIO_0",       11, 17,  0, RaspiPin.GPIO_00), GPIO_1("PCM_CLK/PWM0", 12, 18,  1, RaspiPin.GPIO_01),
        GPIO_2("GPIO_2",       13, 27,  2, RaspiPin.GPIO_02), GRND_3("GND",          14, -1, -1, null),
        GPIO_3("GPIO_3",       15, 22,  3, RaspiPin.GPIO_03), GPIO_4("GPIO_4",       16, 23,  4, RaspiPin.GPIO_04),
        PWR_4("3v3",           17, -1, -1, null),             GPIO_5("GPIO_5",       18, 24,  5, RaspiPin.GPIO_05),
        GPIO_12("SPI0_MOSI",   19, 10, 12, RaspiPin.GPIO_12), GRND_4("GND",          20, -1, -1, null),
        GPIO_13("SPI0_MISO",   21,  9, 13, RaspiPin.GPIO_13), GPIO_6("GPIO_6",       22, 25,  6, RaspiPin.GPIO_06),
        GPIO_14("SPI0_CLK",    23, 11, 14, RaspiPin.GPIO_14), GPIO_10("SPI0_CS0_N",  24,  8, 10, RaspiPin.GPIO_10),
        GRND_5("GND",          25, -1, -1, null),             GPIO_11("SPI0_CS1_N",  26,  7, 11, RaspiPin.GPIO_11),
        SDA0("SDA0",           27, -1, 30, RaspiPin.GPIO_30), SCL0("SCL0",           28, -1, 31, RaspiPin.GPIO_31),
        GPIO_21("GPCLK1",      29,  5, 21, RaspiPin.GPIO_21), GRND_6("GND",          30, -1, -1, null),
        GPIO_22("GPCLK2",      31,  6, 22, RaspiPin.GPIO_22), GPIO_26("PWM0",        32, 12, 26, RaspiPin.GPIO_26),
        GPIO_23("PWM1",        33, 13, 23, RaspiPin.GPIO_23), GRND_7("GND",          34, -1, -1, null),
        GPIO_24("PCM_FS/PWM1", 35, 19, 24, RaspiPin.GPIO_24), GPIO_27("GPIO_27",     36, 16, 27, RaspiPin.GPIO_27),
        GPIO_25("GPIO_25",     37, 26, 25, RaspiPin.GPIO_25), GPIO_28("PCM_DIN",     38, 20, 28, RaspiPin.GPIO_28),
        GRND_8("GND",          39, -1, -1, null),             GPIO_29("PCM_DOUT",    40, 21, 29, RaspiPin.GPIO_29);

        private String pinName; // Pin name
        private int pinNumber;  // Physical, [1..40]
        private int gpio;       // Used by onoff (nodejs), BCM in 'gpio readall', and Javah-io
        private int wiringPi;   // Also used by PI4J
        private Pin pin;

        GPIOPin(String name, int pinNumber, int gpio, int wiring, Pin pin) {
            this.pinName = name;
            this.pinNumber = pinNumber;
            this.gpio = gpio;
            this.wiringPi = wiring;
            this.pin = pin;
        }

        public String pinName() { return this.pinName; }
        public int pinNumber() { return this.pinNumber; }
        public int gpio() { return this.gpio; }
        public int wiringPi() { return this.wiringPi; }
        public Pin pin() { return this.pin; }
    };

    public static Pin getPinByPhysicalNumber(int n) {
        Pin pin = null;
        for (GPIOPin gpioPin : GPIOPin.values()) {
            if (gpioPin.pinNumber() == n) {
                pin = gpioPin.pin();
                break;
            }
        }
        return pin;
    }

    public static int getPhysicalByWiringPiNumber(Pin wPi) {
        int physical = 0;
        for (GPIOPin gpioPin : GPIOPin.values()) {
            if (gpioPin.pin != null && gpioPin.pin.equals(wPi)) {
                physical = gpioPin.pinNumber;
                break;
            }
        }
        return physical;
    }

    public static Pin getPinByWiringPiNumber(int n) {
        Pin pin = null;
        for (GPIOPin gpioPin : GPIOPin.values()) {
            if (gpioPin.wiringPi() == n) {
                pin = gpioPin.pin();
                break;
            }
        }
        return pin;
    }

    public static int getWiringPiNumber(Pin p) {
        int wpi = -1;
        for (GPIOPin gpioPin : GPIOPin.values()) {
            if (gpioPin.pin().equals(p)) {
                wpi = gpioPin.wiringPi();
            }
        }
        return wpi;
    }

    public static Pin getPinByGPIONumber(int n) {
        Pin pin = null;
        for (GPIOPin gpioPin : GPIOPin.values()) {
            if (gpioPin.gpio() == n) {
                pin = gpioPin.pin();
                break;
            }
        }
        return pin;
    }

    public static GPIOPin findByPin(Pin pin) {
        GPIOPin gpio = null;
        for (GPIOPin gpioPin : GPIOPin.values()) {
            if (pin != null && pin.equals(gpioPin.pin())) {
                gpio = gpioPin;
                break;
            }
        }
        return gpio;
    }
    public static GPIOPin findEnumName(String pinName) {
        GPIOPin gpio = null;
        for (GPIOPin gpioPin : GPIOPin.values()) {
            if (pinName != null && (pinName.equals(gpioPin.pinName()) || pinName.equals(gpioPin.toString()))) {
                gpio = gpioPin;
                break;
            }
        }
        return gpio;
    }


    public static void print(String... maps) {
        print(false, maps);
    }
    /**
     *
     * @param maps string array like "xx:text" where xx is the physical number, and text the label to display (5 letters max)
     */
    public static void print(boolean prefixBCMWithGPIO, String... maps) {
        System.out.print(getBuffer(prefixBCMWithGPIO, maps).toString());
    }

    public static StringBuffer getBuffer(boolean prefixBCMWithGPIO, String... maps) {

        StringBuffer sb = new StringBuffer();

        Map<Integer, String> pinMap = null;
        if (maps.length > 0) {
            pinMap = new HashMap<>(maps.length);
            for (String elmt : maps) {
                String[] tuple = elmt.split(":");
                try {
                    int pin = Integer.parseInt(tuple[0]);
                    pinMap.put(pin, tuple[1]);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
        }

        GPIOPin[] values = GPIOPin.values();
        String hr;
        String header;
        String fmt = " %5s |  %s |  %s | %-12s | #%02d || #%02d | %12s | %s  | %s  | %-5s ";
        if (!prefixBCMWithGPIO) {
//	 String hr =            "       |  04 | 07  | GPCLK0       | #07 || #08 |    UART0_TXD | 14  | 15  |";
            hr =            "       +-----+-----+--------------+-----++-----+--------------+-----+-----+";
            header =        "       | BCM | wPi | Name         |  Physical  |         Name | wPi | BCM |";
        } else {
//	 String hr =            "       |  GPIO04 | 07  | GPCLK0       | #07 || #08 |    UART0_TXD | 14  | GPIO15  |";
            hr =            "       +---------+-----+--------------+-----++-----+--------------+-----+---------+";
            header =        "       |     BCM | wPi | Name         |  Physical  |         Name | wPi | BCM     |";
        }
        sb.append(hr + "\n");
        sb.append(header + "\n");
        sb.append(hr + "\n");
        for (int row=0; row<(values.length / 2); row++) {
            String line = String.format(fmt,
                    // Left column
                    (pinMap != null && pinMap.get(values[row * 2].pinNumber()) != null) ? String.valueOf(pinMap.get(values[row * 2].pinNumber())) : " ",
                    values[row * 2].gpio() == -1 ? (prefixBCMWithGPIO ? "      " : "  ") : String.format("%s%02d", prefixBCMWithGPIO ? "GPIO" : "", values[row * 2].gpio()), // BCM
                    values[row * 2].wiringPi() == -1 ? "  " : String.format("%02d", values[row * 2].wiringPi()), // wPI
                    values[row * 2].pinName(),
                    values[row * 2].pinNumber(),
                    // Right column
                    values[1 + (row * 2)].pinNumber(),
                    values[1 + (row * 2)].pinName(),
                    values[1 + (row * 2)].wiringPi() == -1 ? "  " : String.format("%02d", values[1 + (row * 2)].wiringPi()), // wPI
                    values[1 + (row * 2)].gpio() == -1 ? (prefixBCMWithGPIO ? "      " : "  ") : String.format("%s%02d", prefixBCMWithGPIO ? "GPIO" : "", values[1 + (row * 2)].gpio()), // BCM
                    (pinMap != null && pinMap.get(values[1 + (row * 2)].pinNumber()) != null) ? String.valueOf(pinMap.get(values[1 + (row * 2)].pinNumber())) : " ");
            sb.append(line + "\n");
        }
        sb.append(hr + "\n");
        sb.append(header + "\n");
        sb.append(hr + "\n");

        return sb;
    }

    public static void main(String... args) {
        print();

        // List pins
        for (GPIOPin gpioPin : GPIOPin.values()) {
            System.out.println(String.format("%s is named %s", gpioPin.toString(), gpioPin.pinName()));
        }

        System.out.println("\nAs for an MCP3008 (Physical:Label):");
        print("23:CLK", "21:Dout", "19:Din", "24:CS");

        System.out.println("\nAs for an MCP3008, with GPIO prefix (Physical:Label):");
        print(true, "23:CLK", "21:Dout", "19:Din", "24:CS");

        int physical = getPhysicalByWiringPiNumber(RaspiPin.GPIO_29);
        System.out.println(String.format("Physical by WiringPi number: GPIO_29 => #%d", physical)); // Should be #40

        String sdaLabel = String.valueOf(PinUtil.findEnumName("SDA1").pinNumber()) + ":" + "SDA";
        System.out.println(">> SDA Label example : " + sdaLabel);
    }
}