package analogdigitalconverter.mcp3008.sample;

import analogdigitalconverter.mcp3008.MCP3008Reader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
import utils.StringUtils;

import static utils.StringUtils.lpad;

public class MainMCP3008Sample {
	private final static boolean DEBUG = "true".equals(System.getProperty("debug", "false"));
	private static boolean go = true;
	private static int adcChannel =
					MCP3008Reader.MCP3008_input_channels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

	private static final String MISO_PRM_PREFIX = "-miso:";
	private static final String MOSI_PRM_PREFIX = "-mosi:";
	private static final String CLK_PRM_PREFIX  = "-clk:";
	private static final String CS_PRM_PREFIX   = "-cs:";

	private static final String CHANNEL_PREFIX  = "-channel:";

	public static void main(String... args) {

		// Default pins
		Pin miso = PinUtil.GPIOPin.GPIO_13.pin();
		Pin mosi = PinUtil.GPIOPin.GPIO_12.pin();
		Pin clk  = PinUtil.GPIOPin.GPIO_14.pin();
		Pin cs   = PinUtil.GPIOPin.GPIO_10.pin();

		System.out.println(String.format("Usage is java %s %s%d %s%d %s%d %s%d %s%d",
				MainMCP3008Sample.class.getName(),
				MISO_PRM_PREFIX, PinUtil.findByPin(miso).gpio(),
				MOSI_PRM_PREFIX, PinUtil.findByPin(mosi).gpio(),
				CLK_PRM_PREFIX, PinUtil.findByPin(clk).gpio(),
				CS_PRM_PREFIX, PinUtil.findByPin(cs).gpio(),
				CHANNEL_PREFIX, MCP3008Reader.MCP3008_input_channels.CH0.ch()));
		System.out.println("Values above are default values.");
		System.out.println();

		if (args.length > 0) {
			String pinValue = "";
			int pin;
			for (String prm : args) {
				if (prm.startsWith(MISO_PRM_PREFIX)) {
					pinValue = prm.substring(MISO_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						miso = PinUtil.getPinByWiringPiNumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(MOSI_PRM_PREFIX)) {
					pinValue = prm.substring(MOSI_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						mosi = PinUtil.getPinByWiringPiNumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CLK_PRM_PREFIX)) {
					pinValue = prm.substring(CLK_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						clk = PinUtil.getPinByWiringPiNumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CS_PRM_PREFIX)) {
					pinValue = prm.substring(CS_PRM_PREFIX.length());
					try {
						pin = Integer.parseInt(pinValue);
						cs = PinUtil.getPinByWiringPiNumber(pin);
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
					}
				} else if (prm.startsWith(CHANNEL_PREFIX)) {
					String chValue = prm.substring(CHANNEL_PREFIX.length());
					try {
						adcChannel = Integer.parseInt(chValue);
						if (adcChannel > 7 || adcChannel < 0) {
							throw new RuntimeException("Channel in [0..7] please");
						}
					} catch (NumberFormatException nfe) {
						System.err.println(String.format("Bad pin value for %s, must be an integer [%s]", prm, pinValue));
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
				" +---------++---------------------------------------------+\n" +
				" | MCP3008 || Raspberry PI                                |\n" +
				" +---------++------+------------+---------+---------------+\n" +
				" |         || Pin# | Name       | GPIO    | wiringPI/PI4J |\n" +
				" +---------++------+------------+---------+---------------+");
		System.out.println(String.format(" | CLK     || #%02d  | %s | GPIO_%02d | %02d            |",
				PinUtil.findByPin(clk).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(clk).pinName(), 10, " "),
				PinUtil.findByPin(clk).gpio(),
				PinUtil.findByPin(clk).wiringPi()));
		System.out.println(String.format(" | Din     || #%02d  | %s | GPIO_%02d | %02d            |",
				PinUtil.findByPin(miso).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(miso).pinName(), 10, " "),
				PinUtil.findByPin(miso).gpio(),
				PinUtil.findByPin(miso).wiringPi()));
		System.out.println(String.format(" | Dout    || #%02d  | %s | GPIO_%02d | %02d            |",
				PinUtil.findByPin(mosi).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(mosi).pinName(), 10, " "),
				PinUtil.findByPin(mosi).gpio(),
				PinUtil.findByPin(mosi).wiringPi()));
		System.out.println(String.format(" | CS      || #%02d  | %s | GPIO_%02d | %02d            |",
				PinUtil.findByPin(cs).pinNumber(),
				StringUtils.rpad(PinUtil.findByPin(cs).pinName(), 10, " "),
				PinUtil.findByPin(cs).gpio(),
				PinUtil.findByPin(cs).wiringPi()));
		System.out.println(" +---------++------+------------+---------+---------------+");

		MCP3008Reader.initMCP3008(miso, mosi, clk, cs);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Shutting down.");
				go = false;
				synchronized (Thread.currentThread()) {
					Thread.currentThread().notify();
				}
			}
		});
		int lastRead = 0;
		int tolerance = 5;
		while (go) {
			boolean trimPotChanged = false;
			int adc = MCP3008Reader.readMCP3008(adcChannel);
			int postAdjust = Math.abs(adc - lastRead);
			if (postAdjust > tolerance) {
				trimPotChanged = true;
				int volume = (int) (adc / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
				if (DEBUG || trimPotChanged)
					System.out.println("readAdc:" + Integer.toString(adc) +
									" (0x" + lpad(Integer.toString(adc, 16).toUpperCase(), 2, "0") +
									", 0&" + lpad(Integer.toString(adc, 2), 8, "0") + ")");
				System.out.println("Volume:" + volume + "% (" + adc + ")");
				lastRead = adc;
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
		MCP3008Reader.shutdownMCP3008();
	}
}
