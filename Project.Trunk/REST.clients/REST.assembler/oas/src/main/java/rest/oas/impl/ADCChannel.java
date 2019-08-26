package rest.oas.impl;

import analogdigitalconverter.mcp3008.MCP3008Reader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;

public class ADCChannel {
	private int adcChannel =
			MCP3008Reader.MCP3008_input_channels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

	public ADCChannel(int channel) {
		this(9, 10, 11, 8, channel); // Default. Use BCM numbers
	}

	public ADCChannel() {
		this(9, 10, 11, 8); // Default. Use BCM numbers
	}

	public ADCChannel(int miso, int mosi, int clk, int cs) {
		Pin misoPin = PinUtil.getPinByGPIONumber(miso);
		Pin mosiPin = PinUtil.getPinByGPIONumber(mosi);
		Pin clkPin = PinUtil.getPinByGPIONumber(clk);
		Pin csPin = PinUtil.getPinByGPIONumber(cs);

		MCP3008Reader.initMCP3008(misoPin, mosiPin, clkPin, csPin);
	}

	public ADCChannel(int miso, int mosi, int clk, int cs, int channel) {
		Pin misoPin = PinUtil.getPinByGPIONumber(miso);
		Pin mosiPin = PinUtil.getPinByGPIONumber(mosi);
		Pin clkPin = PinUtil.getPinByGPIONumber(clk);
		Pin csPin = PinUtil.getPinByGPIONumber(cs);

		MCP3008Reader.initMCP3008(misoPin, mosiPin, clkPin, csPin);
		this.adcChannel = channel;
	}

	public int readChannel() {
		int adc = MCP3008Reader.readMCP3008(this.adcChannel);
		System.out.println(String.format(">> Reading channel %d => %d", adcChannel, adc));
		return adc; // [0..1023]
	}

	public float readChannelVolume() {
		int adc = readChannel();
		float volume = (float) (adc / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
		return volume;
	}

	public void close() {
		MCP3008Reader.shutdownMCP3008();
	}
}
