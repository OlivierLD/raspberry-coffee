package sensors;

import analogdigitalconverter.mcp.MCPReader;
import com.pi4j.io.gpio.Pin;
import utils.PinUtil;
import utils.gpio.StringToGPIOPin;

/**
 * ADC (MCP3008) reading
 */
public class ADCChannel {

	private boolean simulating = false;

	private int adcChannel =
			MCPReader.MCP3008InputChannels.CH0.ch(); // Between 0 and 7, 8 channels on the MCP3008

	public ADCChannel(int channel) {
		this(9, 10, 11, 8, channel); // Default. Use BCM numbers
	}

	public ADCChannel() {
		this(9, 10, 11, 8); // Default. Use BCM numbers
	}

	public ADCChannel(int miso, int mosi, int clk, int cs) {
		Pin misoPin = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(miso));
		Pin mosiPin = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(mosi));
		Pin clkPin  = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(clk));
		Pin csPin   = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(cs));

		MCPReader.initMCP(misoPin, mosiPin, clkPin, csPin);
	}

	public ADCChannel(int miso, int mosi, int clk, int cs, int channel) {
		Pin misoPin = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(miso));
		Pin mosiPin = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(mosi));
		Pin clkPin  = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(clk));
		Pin csPin   = StringToGPIOPin.stringToGPIOPin(PinUtil.getPinByGPIONumber(cs));

		try {
			MCPReader.initMCP(misoPin, mosiPin, clkPin, csPin);
		} catch (Throwable t) {
			simulating = true;
			System.out.println("Simulating ADCChannel");
		}

		this.adcChannel = channel;
	}

	public int readChannel() {
		int adc = 0;
		if (!simulating) {
			adc = MCPReader.readMCP(this.adcChannel);
		} else {
			adc = (int)Math.round(Math.random() * 1024);
		}
		return adc; // [0..1023]
	}

	public float readChannelVolume() {
		int adc = readChannel();
		float volume = (float) (adc / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
		return volume;
	}

	public void close() {
		if (!simulating) {
			MCPReader.shutdownMCP();
		}
	}
}
