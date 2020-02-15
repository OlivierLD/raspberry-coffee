package rpi.sensors;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;

public class ADCChannel {

	private static boolean simulating = false;

	private static int DEFAULT_CHANNEL = MCPReader.MCP3008InputChannels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008
	private int adcChannel = DEFAULT_CHANNEL;

	public ADCChannel(int channel) {
		this(9, 10, 11, 8, channel); // Default. Use BCM numbers
	}

	public ADCChannel() {
		this(9, 10, 11, 8); // Default. Use BCM numbers
	}

	public ADCChannel(int miso, int mosi, int clk, int cs) {
		this(miso, mosi, clk, cs, DEFAULT_CHANNEL);
	}

	public ADCChannel(int miso, int mosi, int clk, int cs, int channel) {
		Pin misoPin = PinUtil.getPinByGPIONumber(miso);
		Pin mosiPin = PinUtil.getPinByGPIONumber(mosi);
		Pin clkPin = PinUtil.getPinByGPIONumber(clk);
		Pin csPin = PinUtil.getPinByGPIONumber(cs);

		try {
			MCPReader.initMCP(misoPin, mosiPin, clkPin, csPin);
		} catch (Throwable error) {
			System.out.println("Will simulate the ADC...");
			simulating = true;
		}
		this.adcChannel = channel;
	}

	public int readChannel() {
		int adc = 0;
		if (!simulating) {
			adc = MCPReader.readMCP(this.adcChannel);
		} else {
			adc = (int) Math.round(1024 * Math.random());
		}
		System.out.println(String.format(">> Reading channel %d => %d", adcChannel, adc));
		return adc; // [0..1023]
	}

	public float readChannelVolume() {
		int adc = readChannel();
		float volume = (float) (adc / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
		return volume;
	}

	public static void close() {
		if (!simulating) {
			MCPReader.shutdownMCP();
		}
	}
}
