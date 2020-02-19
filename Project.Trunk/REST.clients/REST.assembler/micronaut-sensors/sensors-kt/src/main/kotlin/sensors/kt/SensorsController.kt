package sensors.kt

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import rpi.sensors.ADCChannel
import java.util.logging.Level
import java.util.logging.Logger

@Controller("/sensors")
class SensorsController {

	private val LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)

	private var adcConfiguration: ADCConfiguration? = null
	private var adcChannel: ADCChannel? = null

	constructor (adcConfiguration: ADCConfiguration?) {

		// For info...
		System.getProperties().forEach { prop: Any?, value: Any? -> println(String.format("Prop %s => %s", prop, value)) }

		this.adcConfiguration = adcConfiguration
		if (this.adcConfiguration != null) {
			LOGGER.log(Level.ALL, java.lang.String.format("ADC Config: Channel:%d, MISO:%d, MOSI:%d, CLK:%d, CS:%d",
					this.adcConfiguration?.getChannel(),
					this.adcConfiguration?.getMiso(),
					this.adcConfiguration?.getMosi(),
					this.adcConfiguration?.getClk(),
					this.adcConfiguration?.getCs()))
			this.adcChannel = this.adcConfiguration?.getADCChannel(
					this.adcConfiguration!!.getMiso(),
					this.adcConfiguration!!.getMosi(),
					this.adcConfiguration!!.getClk(),
					this.adcConfiguration!!.getCs(),
					this.adcConfiguration!!.getChannel())
		} else {
			println("SensorsController: Config is null!!")
		}
	}

	@Get("/ambient-light")
  @Produces(MediaType.APPLICATION_JSON)
	fun getLuminosity(): String {
		val light = this.adcChannel!!.readChannelVolume()
		return "{ \"light\": $light }"
	}
}
