package analogdigitalconverter.sample;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
import utils.StringUtils;
import utils.gpio.StringToGPIOPin;

import static utils.StringUtils.lpad;

/**
 * Got one from SparkFun: https://www.sparkfun.com/products/8636
 *
 * TODO Not good. See the python code...
 */
public class MainMCP3002Sample {
	private final static boolean VERBOSE = "true".equals(System.getProperty("verbose", "false"));
	private final static boolean DEBUG = "true".equals(System.getProperty("adc.verbose", "false"));
	private static boolean go = true;
	private static int adcChannel =
					MCPReader.MCP3002InputChannels.CH0.ch(); // Between 0 and 1, 2 channels on the MCP3002

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

		System.out.println(String.format("Usage is java %s %s%d %s%d %s%d %s%d %s%d",
				MainMCP3002Sample.class.getName(),
				MISO_PRM_PREFIX,  PinUtil.findByPin(miso.getName()).gpio(),
				MOSI_PRM_PREFIX,  PinUtil.findByPin(mosi.getName()).gpio(),
				CLK_PRM_PREFIX,   PinUtil.findByPin(clk.getName()).gpio(),
				CS_PRM_PREFIX,    PinUtil.findByPin(cs.getName()).gpio(),
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
						miso = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(MOSI_PRM_PREFIX)) {
					pinValue = prm.substring(MOSI_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						mosi = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CLK_PRM_PREFIX)) {
					pinValue = prm.substring(CLK_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						clk = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CS_PRM_PREFIX)) {
					pinValue = prm.substring(CS_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						cs = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(pin));
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CHANNEL_PREFIX)) {
					String chValue = prm.substring(CHANNEL_PREFIX.length());
					try {
						adcChannel = Integer.parseInt(chValue);
						boolean validChannel = false;
						for (MCPReader.MCP3002InputChannels channel : MCPReader.MCP3002InputChannels.values()) {
							if (channel.ch() == adcChannel) {
								validChannel = true;
								break;
							}
						}
						if (!validChannel) {
							throw new IllegalArgumentException(String.format("Non-suitable channel for MCP3002: %d", adcChannel));
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

		System.out.println(String.format("Reading MCP3002 on channel %d", adcChannel));
		System.out.println(
				" Wiring of the MCP3002-SPI (without power supply):\n" +
						" +---------++-------------------------------------------------+\n" +
						" | MCP3002 || Raspberry Pi                                    |\n" +
						" +---------++------+--------------+------+---------+----------+\n" +
						" |         || Pin# | Name         | Role | GPIO    | wiringPI |\n" +
						" |         ||      |              |      | /BCM    | /PI4J    |\n" +
						" +---------++------+--------------+------+---------+----------+");
		System.out.println(String.format(" | CLK (7) || #%02d  | %s | CLK  | GPIO_%02d | %02d       |",
				PinUtil.findByPin(clk.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(clk.getName()).pinName(), 12, " "),
				PinUtil.findByPin(clk.getName()).gpio(),
				PinUtil.findByPin(clk.getName()).wiringPi()));
		System.out.println(String.format(" | Din (6) || #%02d  | %s | MOSI | GPIO_%02d | %02d       |",
				PinUtil.findByPin(mosi.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(mosi.getName()).pinName(), 12, " "),
				PinUtil.findByPin(mosi.getName()).gpio(),
				PinUtil.findByPin(mosi.getName()).wiringPi()));
		System.out.println(String.format(" | Dout(5) || #%02d  | %s | MISO | GPIO_%02d | %02d       |",
				PinUtil.findByPin(miso.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(miso.getName()).pinName(), 12, " "),
				PinUtil.findByPin(miso.getName()).gpio(),
				PinUtil.findByPin(miso.getName()).wiringPi()));
		System.out.println(String.format(" | CS  (1) || #%02d  | %s | CS   | GPIO_%02d | %02d       |",
				PinUtil.findByPin(cs.getName()).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(cs.getName()).pinName(), 12, " "),
				PinUtil.findByPin(cs.getName()).gpio(),
				PinUtil.findByPin(cs.getName()).wiringPi()));
		System.out.println(" +---------++------+--------------+-----+----------+----------+");
		System.out.println("Raspberry Pi is the Master, MCP3002 is the Slave:");
		System.out.println("- Dout on the MCP3002 goes to MISO on the RPi");
		System.out.println("- Din on the MCP3002 goes to MOSI on the RPi");
		System.out.println("Pins on the MCP3002 are numbered from 1 to 8, beginning top left, counter-clockwise.");

		System.out.println(               "       +------+ ");
		System.out.println(               "   CS -+ 1  8 +- Vdd/Vref ");
		System.out.println(String.format("%s CH0 -+ 2  7 +- CLK ", (adcChannel == 0 ? "*" : " ")));
		System.out.println(String.format("%s CH1 -+ 3  6 +- Dout ", (adcChannel == 1 ? "*" : " ")));
		System.out.println(               "  Vss -+ 4  5 +- Din ");
		System.out.println(               "       +------+ ");

		// Compose mapping for PinUtil
		String[] map = new String[]{
				String.valueOf(PinUtil.findByPin(clk.getName()).pinNumber()) + ":" + "CLK",
				String.valueOf(PinUtil.findByPin(miso.getName()).pinNumber()) + ":" + "Dout",
				String.valueOf(PinUtil.findByPin(mosi.getName()).pinNumber()) + ":" + "Din",
				String.valueOf(PinUtil.findByPin(cs.getName()).pinNumber()) + ":" + "CS"
		};
		PinUtil.print(map);

		MCPReader.initMCP(MCPReader.MCPFlavor.MCP3002, miso, mosi, clk, cs);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Shutting down.");
			go = false;
			synchronized (Thread.currentThread()) {
				Thread.currentThread().notify();
			}
		}, "Shutdown Hook"));
		int lastRead = 0;
		int tolerance = 5;
		boolean first = true;
		while (go) {
			if (VERBOSE) {
				System.out.println("Reading channel " + adcChannel);
			}
			int adc = MCPReader.readMCP(adcChannel);
			if (VERBOSE) {
				System.out.println(String.format("From ch %d: %d", adcChannel, adc));
			}
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
