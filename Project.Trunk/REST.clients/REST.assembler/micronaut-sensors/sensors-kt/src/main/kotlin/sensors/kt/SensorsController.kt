package sensors.kt

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*

@Controller("/sensors")
class SensorsController {

	@Get("/ambient-light")
  @Produces(MediaType.APPLICATION_JSON)
	fun getLuminosity(): String {
		val light = 12.34
		return "{ \"light\": $light }"
	}
}
