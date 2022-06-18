import i2c.sensor.BME280
import com.pi4j.system.SystemInfo
/*
 * Requires a BME280
 * If you see errors about that, check your sensors...
 */
object Scala_BME280 {
  def main(args: Array[String]): Unit = {
    println("Hello, Scala world! Reading sensors.")
    val bme280  = new BME280
    try {
      val temp  = bme280.readTemperature
      val press = bme280.readPressure / 100
      val hum   = bme280.readHumidity
      println(s"CPU Temperature   :  ${ SystemInfo.getCpuTemperature }\u00baC")
      println(s"Temp:$temp \u00baC, Press:$press hPa, Humidity:$hum %")
    } catch {
      case ex: Exception =>
        println(ex.toString)
    }
  }
}
