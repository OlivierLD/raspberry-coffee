package analogdigitalconverter.mcp3008.sample;

import analogdigitalconverter.mcp3008.MCP3008Reader;
import analogdigitalconverter.mcp3008.MCP3008Reader.MCP3008_input_channels;
import com.pi4j.io.gpio.RaspiPin;

import static utils.StringUtils.lpad;

public class MainMCP3008Sample {
	private final static boolean DEBUG = "true".equals(System.getProperty("debug", "false"));
	private static boolean go = true;
	private static int ADC_CHANNEL =
					MCP3008Reader.MCP3008_input_channels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

	public static void main(String[] args) {
		System.out.println(String.format("Reading MCP3008 on channel %d", ADC_CHANNEL));
		System.out.println("Using the following pins:");
		String pinout =
				" Wiring of the MCP3008-SPI (without power supply):\n" +
				" +---------++---------------------------------------------+\n" +
				" | MCP3008 || Raspberry PI                                |\n" +
				" +---------++------+------------+---------+---------------+\n" +
				" |         || Pin# | Name       | GPIO    | wiringPI/PI4J |\n" +
				" +---------++------+------------+---------+---------------+\n" +
				" | CLK     ||  #23 | SPI0_CLK   | GPIO_11 |  14           |\n" +
				" | Din     ||  #21 | SPI0_MISO  | GPIO_9  |  13           |\n" +
				" | Dout    ||  #19 | SPI0_MOSI  | GPIO_10 |  12           |\n" +
				" | CS      ||  #24 | SPI0_CE0_N | GPIO_8  |  10           |\n" +
				" +---------++------+------------+---------+---------------+";
		System.out.println(pinout);
		//                        spiMiso,          spiMosi,          spiClk,           spiCs
		MCP3008Reader.initMCP3008(RaspiPin.GPIO_13, RaspiPin.GPIO_12, RaspiPin.GPIO_14, RaspiPin.GPIO_10);

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
			int adc = MCP3008Reader.readMCP3008(ADC_CHANNEL);
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
