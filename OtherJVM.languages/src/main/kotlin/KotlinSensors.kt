import i2c.sensor.BME280

fun main(args: Array<String>) {

    val bme280 = BME280()
    try {
        val temp  = bme280.readTemperature()
        val press = bme280.readPressure() / 100
        val hum   = bme280.readHumidity()

        println("Temp:$temp \u00baC, Press:$press hPa, Hum:$hum %")
    } catch (ex: Exception) {
        println(ex.toString())
    }
}