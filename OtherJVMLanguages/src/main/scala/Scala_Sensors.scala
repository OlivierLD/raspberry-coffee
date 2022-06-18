import i2c.sensor.{HTU21DF, BMP180}
import com.pi4j.system.SystemInfo
/*
 * Requires a BMP180, on I2C address 0x77
 *          a HTU21DF, on I2C address 0x40
 * If you see errors about that, check your sensors...
 */
object Scala_Sensors {
  def main(args: Array[String]): Unit = {
    println("Hello, Scala world! Reading sensors.")
    val bmp180  = new BMP180
    val htu21df = new HTU21DF
    try {
      val temp  = bmp180.readTemperature
      val press = bmp180.readPressure / 100
      println(s"CPU Temperature   :  ${ SystemInfo.getCpuTemperature }\u00baC")
      println(s"Temp:$temp \u00baC, Press:$press hPa")
    } catch {
      case ex: Exception =>
        println(ex.toString)
    }
    try {
      htu21df.begin
      val hum = htu21df.readHumidity
      htu21df.close
      println(s"Humidity:$hum %")
    } catch {
      case ex: Exception =>
//    println(ex.toString)
        ex.printStackTrace
    }
  }
}
