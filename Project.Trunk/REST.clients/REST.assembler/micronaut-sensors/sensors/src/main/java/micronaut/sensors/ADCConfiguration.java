package micronaut.sensors;

import analogdigitalconverter.mcp.MCPReader;
import io.micronaut.context.annotation.ConfigurationProperties;
import rpi.sensors.ADCChannel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@ConfigurationProperties("adc")
public class ADCConfiguration {
	private int clk;
	private int miso;
	private int mosi;
	private int cs;
	private int channel;

	private ADCChannel adcChannel = null;

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

	@PostConstruct
	public void initialize() {
		System.out.println("---------------------------------");
		System.out.println("PostConstruct in ADCConfiguration");
		System.out.println("---------------------------------");
	}

	@PreDestroy
	public void close() { // Invoked when the Docker container is stopped
		System.out.println("---------------------------------");
		System.out.println("PreDestroy in ADCConfiguration");
		System.out.println("---------------------------------");
		if (this.adcChannel != null) {
			ADCChannel.close();
		}
	}

	public ADCChannel getADCChannel(int miso, int mosi, int clk, int cs, int channel) {
		this.adcChannel =  new ADCChannel(miso, mosi, clk, cs, channel);
		return this.adcChannel;
	}

}
