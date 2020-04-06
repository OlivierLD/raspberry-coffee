package analogdigitalconverter.sample;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
import utils.StringUtils;

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
		Pin miso = PinUtil.GPIOPin.GPIO_13.pin();
		Pin mosi = PinUtil.GPIOPin.GPIO_12.pin();
		Pin clk  = PinUtil.GPIOPin.GPIO_14.pin();
		Pin cs   = PinUtil.GPIOPin.GPIO_10.pin();

		System.out.println(String.format("Usage is java %s %s%d %s%d %s%d %s%d %s%d",
				MainMCP3008Sample.class.getName(),
				MISO_PRM_PREFIX,  PinUtil.findByPin(miso).gpio(),
				MOSI_PRM_PREFIX,  PinUtil.findByPin(mosi).gpio(),
				CLK_PRM_PREFIX,   PinUtil.findByPin(clk).gpio(),
				CS_PRM_PREFIX,    PinUtil.findByPin(cs).gpio(),
				CHANNEL_PREFIX,   adcChannel));
		System.out.println("Values above are default values (GPIO/BCM numbers).");
		System.out.println();

		if (args.length > 0) {
			String pinValue = "";
			int pin;
			for (String prm : args) {
				if (prm.startsWith(MISO_PRM_PREFIX)) {
					pinValue = prm.substring(MISO_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						miso = PinUtil.getPinByGPIONumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(MOSI_PRM_PREFIX)) {
					pinValue = prm.substring(MOSI_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						mosi = PinUtil.getPinByGPIONumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CLK_PRM_PREFIX)) {
					pinValue = prm.substring(CLK_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						clk = PinUtil.getPinByGPIONumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CS_PRM_PREFIX)) {
					pinValue = prm.substring(CS_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						cs = PinUtil.getPinByGPIONumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
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
						System.err.println(String.format("Bad value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else {
					// What?
					System.err.println(String.format("Un-managed prm: %s", prm));
				}
			}
		}

		System.out.println(String.format("Reading MCP3008 on channel %d", adcChannel));
		System.out.println(
				" Wiring of the MCP3008-SPI (without power supply):\n" +
						" +---------++-------------------------------------------------+\n" +
						" | MCP3008 || Raspberry Pi                                    |\n" +
						" +---------++------+--------------+------+---------+----------+\n" +
						" |         || Pin# | Name         | Role | GPIO    | wiringPI |\n" +
						" |         ||      |              |      | /BCM    | /PI4J    |\n" +
						" +---------++------+--------------+------+---------+----------+");
		System.out.println(String.format(" | CLK (13)|| #%02d  | %s | CLK  | GPIO_%02d | %02d       |",
				PinUtil.findByPin(clk).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(clk).pinName(), 12, " "),
				PinUtil.findByPin(clk).gpio(),
				PinUtil.findByPin(clk).wiringPi()));
		System.out.println(String.format(" | Din (11)|| #%02d  | %s | MOSI | GPIO_%02d | %02d       |",
				PinUtil.findByPin(mosi).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(mosi).pinName(), 12, " "),
				PinUtil.findByPin(mosi).gpio(),
				PinUtil.findByPin(mosi).wiringPi()));
		System.out.println(String.format(" | Dout(12)|| #%02d  | %s | MISO | GPIO_%02d | %02d       |",
				PinUtil.findByPin(miso).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(miso).pinName(), 12, " "),
				PinUtil.findByPin(miso).gpio(),
				PinUtil.findByPin(miso).wiringPi()));
		System.out.println(String.format(" | CS  (10)|| #%02d  | %s | CS   | GPIO_%02d | %02d       |",
				PinUtil.findByPin(cs).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(cs).pinName(), 12, " "),
				PinUtil.findByPin(cs).gpio(),
				PinUtil.findByPin(cs).wiringPi()));
		System.out.println(" +---------++------+--------------+-----+----------+----------+");
		System.out.println("Raspberry Pi is the Master, MCP3008 is the Slave:");
		System.out.println("- Dout on the MCP3008 goes to MISO on the RPi");
		System.out.println("- Din on the MCP3008 goes to MOSI on the RPi");
		System.out.println("Pins on the MCP3008 are numbered from 1 to 16, beginning top left, counter-clockwise.");
		System.out.println(               "       +--------+ ");
		System.out.println(String.format("%s CH0 -+  1  16 +- Vdd ",  (adcChannel == 0 ? "*" : " ")));
		System.out.println(String.format("%s CH1 -+  2  15 +- Vref ", (adcChannel == 1 ? "*" : " ")));
		System.out.println(String.format("%s CH2 -+  3  14 +- aGnd ", (adcChannel == 2 ? "*" : " ")));
		System.out.println(String.format("%s CH3 -+  4  13 +- CLK ",  (adcChannel == 3 ? "*" : " ")));
		System.out.println(String.format("%s CH4 -+  5  12 +- Dout ", (adcChannel == 4 ? "*" : " ")));
		System.out.println(String.format("%s CH5 -+  6  11 +- Din ",  (adcChannel == 5 ? "*" : " ")));
		System.out.println(String.format("%s CH6 -+  7  10 +- CS ",   (adcChannel == 6 ? "*" : " ")));
		System.out.println(String.format("%s CH7 -+  8   9 +- dGnd ", (adcChannel == 7 ? "*" : " ")));
		System.out.println(               "       +--------+ ");

		// Compose mapping for PinUtil
		String[] map = new String[4];
		map[0] = String.valueOf(PinUtil.findByPin(clk).pinNumber()) + ":" + "CLK";
		map[1] = String.valueOf(PinUtil.findByPin(miso).pinNumber()) + ":" + "Dout";
		map[2] = String.valueOf(PinUtil.findByPin(mosi).pinNumber()) + ":" + "Din";
		map[3] = String.valueOf(PinUtil.findByPin(cs).pinNumber()) + ":" + "CS";

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
					System.out.println("readAdc:" + Integer.toString(adc) +
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
