package utils;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class PinUtil {
	// Disposed as on the board
	public enum GPIOPin {
		//      Name            #  IO WiPi                            Name            #  IO WiPi
		/* 3v3, 1 */                                          /* 5v0, 2 */
		GPIO_8("SDA1",          3,  2,  8, RaspiPin.GPIO_08), /* 5v0, 4 */
		GPIO_9("SCL1",          5,  3,  9, RaspiPin.GPIO_09), /* GND, 6 */
		GPIO_7("GPCLK0",        7,  4,  7, RaspiPin.GPIO_07), GPIO_15("UART0_TXD",    8, 14, 15, RaspiPin.GPIO_15),
		/* GND, 9 */                                          GPIO_16("UART0_RXD",   10, 15, 16, RaspiPin.GPIO_16),
		GPIO_0("GPIO_0",       11, 17,  0, RaspiPin.GPIO_00), GPIO_1("PCM_CLK/PWM0", 12, 18,  1, RaspiPin.GPIO_01),
		GPIO_2("GPIO_2",       13, 27,  2, RaspiPin.GPIO_02), /* GND, 14 */
		GPIO_3("GPIO_3",       15, 22,  3, RaspiPin.GPIO_03), GPIO_4("GPIO_4",       16, 23,  4, RaspiPin.GPIO_04),
		/* 3v3, 17 */                                         GPIO_5("GPIO_5",       18, 24,  5, RaspiPin.GPIO_05),
		GPIO_12("SPI0_MOSI",   19, 10, 12, RaspiPin.GPIO_12), /* GND, 20 */
		GPIO_13("SPI0_MISO",   21,  9, 13, RaspiPin.GPIO_13), GPIO_6("GPIO_6",       22, 25,  6, RaspiPin.GPIO_06),
		GPIO_14("SPI0_CLK",    23, 11, 14, RaspiPin.GPIO_14), GPIO_10("SPI0_CS0_N",  24,  8, 10, RaspiPin.GPIO_10),
		/* GND, 25 */                                         GPIO_11("SPI0_CS1_N",  26,  7, 11, RaspiPin.GPIO_11),
		SDA0("SDA0",           27, -1, 30, RaspiPin.GPIO_30), SCL0("SCL0",           28, -1, 31, RaspiPin.GPIO_31),
		GPIO_21("GPCLK1",      29,  5, 21, RaspiPin.GPIO_21), /* GND, 30 */
		GPIO_22("GPCLK2",      31,  6, 22, RaspiPin.GPIO_22), GPIO_26("PWM0",        32, 12, 26, RaspiPin.GPIO_26),
		GPIO_23("PWM1",        33, 13, 23, RaspiPin.GPIO_23), /* GND, 34 */
		GPIO_24("PCM_FS/PWM1", 35, 19, 24, RaspiPin.GPIO_24), GPIO_27("GPIO_27",     36, 16, 27, RaspiPin.GPIO_27),
		GPIO_25("GPIO_25",     37, 26, 25, RaspiPin.GPIO_25), GPIO_28("PCM_DIN",     38, 20, 28, RaspiPin.GPIO_28),
		/* GND, 39 */                                         GPIO_29("PCM_DOUT",    40, 21, 29, RaspiPin.GPIO_29);

		private String pinName; // Pin name
		private int pinNumber;  // Physical
		private int gpio;       // Used by onoff (nodejs) anf Javah-io
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

	public static GPIOPin findByPin(Pin pin) {
		GPIOPin gpio = null;
		for (GPIOPin gpioPin : GPIOPin.values()) {
			if (gpioPin.pin().equals(pin)) {
				gpio = gpioPin;
				break;
			}
		}
		return gpio;
	}
}
