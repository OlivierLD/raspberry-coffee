package analogdigitalconverter.sample;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
import utils.StringUtils;
import utils.gpio.StringToGPIOPin;

import static utils.StringUtils.lpad;

public class MainMCP3008Sample {
	private final static boolean DEBUG = "true".equals(System.getProperty("adc.verbose", "false"));
	private static boolean go = true;
	private static int adcChannel =
					MCPReader.MCP3008InputChannels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

	private static final String MISO_PRM_PREFIX = "--miso:";
	private static final String MOSI_PRM_PREFIX = "--mosi:";
	private static final String CLK_PRM_PREFIX  =  "--clk:";
	private static final String CS_PRM_PREFIX   =   "--cs:";

	private static final String CHANNEL_PREFIX  = "--channel:";

	public static void main(String... args) {

		// Default pins
		Pin miso = StringToGPIOPin.stringToGPIOPin(PinUtil.GPIOPin.GPIO_13.pin());
		Pin mosi = StringToGPIOPin.stringToGPIOPin(PinUtil.GPIOPin.GPIO_12.pin());
		Pin clk  = StringToGPIOPin.stringToGPIOPin(PinUtil.GPIOPin.GPIO_14.pin());
		Pin cs   = StringToGPIOPin.stringToGPIOPin(PinUtil.GPIOPin.GPIO_10.pin());

		System.out.printf("Usage is java %s %s%d %s%d %s%d %s%d %s%d\n",
				MainMCP3008Sample.class.getName(),
				MISO_PRM_PREFIX,  PinUtil.findByPin(miso.getName()).gpio(),
				MOSI_PRM_PREFIX,  PinUtil.findByPin(mosi.getName()).gpio(),
				CLK_PRM_PREFIX,   PinUtil.findByPin(clk.getName()).gpio(),
				CS_PRM_PREFIX,    PinUtil.findByPin(cs.getName()).gpio(),
				CHANNEL_PREFIX,   adcChannel);
		System.out.println("Values above are default values (GPIO/BCM numbers).");
		System.out.printf("Pins: %s (%s) %s (%s) %s (%s) %s (%s)\n",
				PinUtil.findByPin(miso.getName()).gpio(), miso.getName(),
				PinUtil.findByPin(mosi.getName()).gpio(), mosi.getName(),
				PinUtil.findByPin(clk.getName()).gpio(), clk.getName(),
				PinUtil.findByPin(cs.getName()).gpio(), cs.getName());
		System.out.println();

		if (args.length > 0) {
			String pinValue = "";
			int pin;
			for (String prm : args) {
				if (prm.startsWith(MISO_PRM_PREFIX)) {
					pinValue = prm.substring(MISO_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						miso = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.printf("Bad pin value for %s, must be an integer [%s]\n", prm, pinValue);
					}
				} else if (prm.startsWith(MOSI_PRM_PREFIX)) {
					pinValue = prm.substring(MOSI_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						mosi = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.printf("Bad pin value for %s, must be an integer [%s]\n", prm, pinValue);
					}
				} else if (prm.startsWith(CLK_PRM_PREFIX)) {
					pinValue = prm.substring(CLK_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						clk = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.printf("Bad pin value for %s, must be an integer [%s]\n", prm, pinValue);
					}
				} else if (prm.startsWith(CS_PRM_PREFIX)) {
					pinValue = prm.substring(CS_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						cs = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.printf("Bad pin value for %s, must be an integer [%s]\n", prm, pinValue);
					}
				} else if (prm.startsWith(CHANNEL_PREFIX)) {
					String chValue = prm.substring(CHANNEL_PREFIX.length());
					try {
						adcChannel = Integer.parseInt(chValue);
						boolean validChannel = false;
						for (MCPReader.MCP3008InputChannels channel : MCPReader.MCP3008InputChannels.values()) {
							if (channel.ch() == adcChannel) {
								validChannel = true;
								break;
							}
						}
						if (!validChannel) {
							throw new IllegalArgumentException(String.format("Non-suitable channel for MCP3008: %d", adcChannel));
						}
					} catch (NumberFormatException nfe) {
						System.err.printf("Bad value for %s, must be an integer [%s]\n", prm, pinValue);
					}
				} else {
					// What?
					System.err.printf("Un-managed prm: %s\n", prm);
				}
			}
		}

		System.out.printf("Reading MCP3008 on channel %d\n", adcChannel);
		System.out.println(
				" Wiring of the MCP3008-SPI (without power supply):\n" +
						" +---------++-------------------------------------------------+\n" +
						" | MCP3008 || Raspberry Pi                                    |\n" +
						" +---------++------+--------------+------+---------+----------+\n" +
						" |         || Pin# | Name         | Role | GPIO    | wiringPI |\n" +
						" |         ||      |              |      | /BCM    | /PI4J    |\n" +
						" +---------++------+--------------+------+---------+----------+");
		System.out.printf(" | CLK (13)|| #%02d  | %s | CLK  | GPIO_%02d | %02d       |\n",
				PinUtil.findByPin(clk.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(clk.getName()).pinName(), 12, " "),
				PinUtil.findByPin(clk.getName()).gpio(),
				PinUtil.findByPin(clk.getName()).wiringPi());
		System.out.printf(" | Din (11)|| #%02d  | %s | MOSI | GPIO_%02d | %02d       |\n",
				PinUtil.findByPin(mosi.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(mosi.getName()).pinName(), 12, " "),
				PinUtil.findByPin(mosi.getName()).gpio(),
				PinUtil.findByPin(mosi.getName()).wiringPi());
		System.out.printf(" | Dout(12)|| #%02d  | %s | MISO | GPIO_%02d | %02d       |\n",
				PinUtil.findByPin(miso.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(miso.getName()).pinName(), 12, " "),
				PinUtil.findByPin(miso.getName()).gpio(),
				PinUtil.findByPin(miso.getName()).wiringPi());
		System.out.printf(" | CS  (10)|| #%02d  | %s | CS   | GPIO_%02d | %02d       |\n",
				PinUtil.findByPin(cs.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(cs.getName()).pinName(), 12, " "),
				PinUtil.findByPin(cs.getName()).gpio(),
				PinUtil.findByPin(cs.getName()).wiringPi());
		System.out.println(" +---------++------+--------------+-----+----------+----------+");
		System.out.println("Raspberry Pi is the Master, MCP3008 is the Slave:");
		System.out.println("- Dout on the MCP3008 goes to MISO on the RPi");
		System.out.println("- Din on the MCP3008 goes to MOSI on the RPi");
		System.out.println("Pins on the MCP3008 are numbered from 1 to 16, beginning top left, counter-clockwise.");
		System.out.println(               "       +--------+ ");
		System.out.printf("%s CH0 -+  1  16 +- Vdd \n",  (adcChannel == 0 ? "*" : " "));
		System.out.printf("%s CH1 -+  2  15 +- Vref \n", (adcChannel == 1 ? "*" : " "));
		System.out.printf("%s CH2 -+  3  14 +- aGnd \n", (adcChannel == 2 ? "*" : " "));
		System.out.printf("%s CH3 -+  4  13 +- CLK \n",  (adcChannel == 3 ? "*" : " "));
		System.out.printf("%s CH4 -+  5  12 +- Dout \n", (adcChannel == 4 ? "*" : " "));
		System.out.printf("%s CH5 -+  6  11 +- Din \n",  (adcChannel == 5 ? "*" : " "));
		System.out.printf("%s CH6 -+  7  10 +- CS \n",   (adcChannel == 6 ? "*" : " "));
		System.out.printf("%s CH7 -+  8   9 +- dGnd \n", (adcChannel == 7 ? "*" : " "));
		System.out.println(               "       +--------+ ");

		// Compose mapping for PinUtil
		String[] map = new String[]{
				(PinUtil.findByPin(clk.getName()).pinNumber()) + ":" + "CLK",
				(PinUtil.findByPin(miso.getName()).pinNumber()) + ":" + "Dout",
				(PinUtil.findByPin(mosi.getName()).pinNumber()) + ":" + "Din",
				(PinUtil.findByPin(cs.getName()).pinNumber()) + ":" + "CS"
		};
		PinUtil.print(map);

		MCPReader.initMCP(MCPReader.MCPFlavor.MCP3008, miso, mosi, clk, cs);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down.");
			go = false;
			synchronized (Thread.currentThread()) {
				Thread.currentThread().notify();
			}
		}, "Shutdown Hook"));
		int lastRead = 0;
		int tolerance = 5; // TODO  Make this a parameter?
		boolean first = true;
		while (go) {
	//	System.out.println("Reading channel " + adcChannel);
			int adc = MCPReader.readMCP(adcChannel);
	//	System.out.println(String.format("From ch %d: %d", adcChannel, adc));
			int postAdjust = Math.abs(adc - lastRead);
			if (first || postAdjust > tolerance) {
				int volume = (int) (adc / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				if (DEBUG) {
					System.out.println("readAdc:" + adc +
							" (0x" + lpad(Integer.toString(adc, 16).toUpperCase(), 2, "0") +
							", 0&" + lpad(Integer.toString(adc, 2), 8, "0") + ")");
				}
				System.out.println("Volume:" + volume + "% (" + adc + ")");
				lastRead = adc;
				first = false;
			}
			try {
				synchronized (Thread.currentThread()) {
					Thread.currentThread().wait(100L);
				}
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		System.out.println("Bye, freeing resources.");
		MCPReader.shutdownMCP();
	}
}
