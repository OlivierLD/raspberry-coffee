import com.pi4j.system.SystemInfo
import i2c.sensor.{HTU21DF, BMP180, BME280}

println("Reading sensors")
val bmp180  = new BMP180
val bme280  = new BME280
val htu21df = new HTU21DF
try {
  val temp  = bmp180.readTemperature
  val press = bmp180.readPressure / 100
  println(s"CPU Temperature   :  ${SystemInfo.getCpuTemperature}\u00baC")
  println(s"Temp:${temp}\u00baC, Press:${press} hPa")
} catch {
  case ex: Exception => {
    println(ex.toString)
  }
}
try {
  htu21df.begin
  val hum = htu21df.readHumidity
  htu21df.close
  println(s"Humidity:${ hum } %")
} catch {
  case ex: Exception => {
    println(ex.toString)
  }
}
try {
  val temp  = bme280.readTemperature
  val press = bme280.readPressure / 100
  val hum = bme280.readHumidity
  println(s"CPU Temperature   :  ${SystemInfo.getCpuTemperature}\u00baC")
  println(s"Temp:${temp}\u00baC, Press:${press} hPa, Hum:${hum} %")
} catch {
  case ex: Exception => {
    println(ex.toString)
  }
}
