package sensors.sparkfunsoilhumiditysensor;

import analogdigitalconverter.mcp.MCPReader;
import main.MCP3008;
import utils.PinUtil;

import java.util.function.Supplier;
import java.util.logging.Level;

import static utils.StringUtils.lpad;

public class MCP3008Wrapper {
	private boolean simulating = false;
	private int channel = 0;
	private Supplier<Double> humiditySimulator = null;

	private boolean debug = "true".equals(System.getProperty("mcp3008.debug", "false"));

	private static MCP3008Wrapper instance = null;

	private MCP3008Wrapper() {}

	public static MCP3008Wrapper init(int misoPin, int mosiPin, int clkPin, int csPin, int channel) {
		return init(misoPin, mosiPin, clkPin, csPin, channel, false);
	}
	public static MCP3008Wrapper init(int misoPin, int mosiPin, int clkPin, int csPin, int channel, boolean debug) {
		if (instance == null) {
			instance = new MCP3008Wrapper();
		}
		instance.channel = channel;
		instance.debug = debug;
		try {
			MCPReader.initMCP(
					PinUtil.getPinByGPIONumber(misoPin),
					PinUtil.getPinByGPIONumber(mosiPin),
					PinUtil.getPinByGPIONumber(clkPin),
					PinUtil.getPinByGPIONumber(csPin));
		} catch (UnsatisfiedLinkError ule) {
			// Not on a Pi?
			instance.simulating = true;
			MCP3008.getLogger().log(Level.ALL, "Not on a Pi?", ule);
		}
		return instance;
	}

	public void setSimulators(Supplier<Double> humSimulator) {
		this.humiditySimulator = humSimulator;
	}

	public void shutdown() {
		if (!this.isSimulating()) {
			MCPReader.shutdownMCP();
		}
	}

	public double readHumidity() {
		if (!this.simulating) {
			return readVolume(this.channel);
		} else {
			return this.humiditySimulator.get();
		}
	}

	public int readVolume(int channel) {

		int volume = 0;
		//	System.out.println("Reading channel " + adcChannel);
		int adc = MCPReader.readMCP(channel);
		//	System.out.println(String.format("From ch %d: %d", adcChannel, adc));
		volume = (int)(adc / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
		if (debug) {
			System.out.println("readAdc:" + Integer.toString(adc) +
					" (0x" + lpad(Integer.toString(adc, 16).toUpperCase(), 2, "0") +
					", 0&" + lpad(Integer.toString(adc, 2), 8, "0") + ")");
		}
		System.out.println(String.format("Volume: %03d%% (%04d) => %.03f V",
				volume,
				adc,
				(3.3 * (adc / 1023.0))));  // Volts
		return volume;
	}

	public boolean isSimulating() {
		return this.simulating;
	}
}
