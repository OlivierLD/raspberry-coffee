package sensors.kt

import io.micronaut.context.annotation.ConfigurationProperties
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import rpi.sensors.ADCChannel


@ConfigurationProperties("adc")
class ADCConfiguration {
	private var clk = 0
	private var miso = 0
	private var mosi = 0
	private var cs = 0
	private var channel = 0

	private var adcChannel: ADCChannel? = null

	fun ADCConfiguration() {}

	fun getClk(): Int {
		return clk
	}

	fun setClk(clk: Int) {
		this.clk = clk
	}

	fun getMiso(): Int {
		return miso
	}

	fun setMiso(miso: Int) {
		this.miso = miso
	}

	fun getMosi(): Int {
		return mosi
	}

	fun setMosi(mosi: Int) {
		this.mosi = mosi
	}

	fun getCs(): Int {
		return cs
	}

	fun setCs(cs: Int) {
		this.cs = cs
	}

	fun getChannel(): Int {
		return channel
	}

	fun setChannel(channel: Int) {
		this.channel = channel
	}

	@PostConstruct
	fun initialize() {
		println("---------------------------------")
		println("PostConstruct in ADCConfiguration")
		println("---------------------------------")
	}

	@PreDestroy
	fun close() { // Invoked when the Docker container is stopped
		println("---------------------------------")
		println("PreDestroy in ADCConfiguration")
		println("---------------------------------")
		if (this.adcChannel != null) {
			ADCChannel.close()
		}
	}

	fun getADCChannel(miso: Int, mosi: Int, clk: Int, cs: Int, channel: Int): ADCChannel? {
		this.adcChannel = ADCChannel(miso, mosi, clk, cs, channel)
		return this.adcChannel
	}

}
