package micronaut.sensors;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("adc")
public class ADCConfiguration {
	private int clk;
	private int miso;
	private int mosi;
	private int cs;
	private int channel;

	public ADCConfiguration() {}

	public int getClk() {
		return clk;
	}

	public void setClk(int clk) {
		this.clk = clk;
	}

	public int getMiso() {
		return miso;
	}

	public void setMiso(int miso) {
		this.miso = miso;
	}

	public int getMosi() {
		return mosi;
	}

	public void setMosi(int mosi) {
		this.mosi = mosi;
	}

	public int getCs() {
		return cs;
	}

	public void setCs(int cs) {
		this.cs = cs;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}
}
